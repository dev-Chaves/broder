/**
 * Teste k6 de carga para validar alarmes do Broder:
 * - HTTP 5xx Error Rate
 * - HTTP 4xx Error Rate
 * - CPU Usage High
 * - Memory Heap High
 *
 * Fluxo:
 * 1. Setup: cria 4 alarmes no Broder com thresholds baixos (garantem disparo)
 *            e pré-aloca memória na aplicação de referência.
 * 2. Stress: 4 cenários paralelos bombardeiam a ref app:
 *    - /error-500  (gera 5xx)
 *    - /error-400  (gera 4xx)
 *    - /cpu        (eleva process_cpu_usage)
 *    - /memory     (eleva heap usage)
 * 3. Verify: após 120s de stress, verifica se todos os alarmes estão FIRING.
 * 4. Teardown: remove alarmes e limpa memória da ref app.
 *
 * Execução:
 *   # Subir a stack (Broder + Prometheus + ref app)
 *   cd api-prometheus && docker compose up -d
 *   cd ../broder && ./mvnw quarkus:dev
 *
 *   # Rodar o teste
 *   cd broder/k6-tests
 *   k6 run alarm-stress-test.js
 *
 * Variáveis de ambiente:
 *   BRODER_BASE_URL  (padrão: http://localhost:8080)
 *   REF_APP_URL      (padrão: http://localhost:8181)
 *   WAIT_SECONDS     (padrão: 25)
 */

import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter, Rate } from 'k6/metrics';
import { BASE_URL, HEADERS, generateUniqueName } from './config.js';

const REF_APP_URL = __ENV.REF_APP_URL || 'http://localhost:8181';
const WAIT_SECONDS = parseInt(__ENV.WAIT_SECONDS || '25');
const DELETE_RETRIES = parseInt(__ENV.DELETE_RETRIES || '3');
const DELETE_RETRY_DELAY = parseFloat(__ENV.DELETE_RETRY_DELAY || '1');

const alarmsFiringTotal = new Counter('alarms_firing_total');
const alarmsCheckSuccess = new Rate('alarms_check_success');
const historyCheckSuccess = new Rate('history_check_success');
const deleteRetries = new Counter('alarm_delete_retries');

export const options = {
    scenarios: {
        stress_5xx: {
            executor: 'constant-vus',
            vus: 5,
            duration: '150s',
            exec: 'stress5xx',
            startTime: '0s',
        },
        stress_4xx: {
            executor: 'constant-vus',
            vus: 5,
            duration: '150s',
            exec: 'stress4xx',
            startTime: '0s',
        },
        stress_cpu: {
            executor: 'constant-vus',
            vus: 10,
            duration: '150s',
            exec: 'stressCpu',
            startTime: '0s',
        },
        stress_memory: {
            executor: 'constant-vus',
            vus: 2,
            duration: '150s',
            exec: 'stressMemory',
            startTime: '0s',
        },
        verify: {
            executor: 'per-vu-iterations',
            vus: 1,
            iterations: 1,
            exec: 'verifyAlarms',
            startTime: '120s',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<3000'],
        http_req_failed: ['rate<0.15'],
        alarms_check_success: ['rate>0.8'],
        history_check_success: ['rate>0.8'],
    },
};

/* ============================================================
   HELPERS
   ============================================================ */

function createAlarm(namePrefix, query, threshold) {
    const payload = JSON.stringify({
        name: generateUniqueName(namePrefix),
        description: `Alarme de stress - ${namePrefix}`,
        query: query,
        comparison: '>',
        threshold: threshold,
        evaluationIntervalSeconds: 20,
        severity: 'HIGH',
        category: 'load_test',
    });

    const res = http.post(`${BASE_URL}/alarms`, payload, { headers: HEADERS });
    check(res, {
        [`alarm ${namePrefix} created`]: (r) => r.status === 201,
    });

    if (res.status === 201) {
        return JSON.parse(res.body).id;
    }
    console.error(`Falha ao criar alarme ${namePrefix}: ${res.status} ${res.body}`);
    return null;
}

function deleteWithRetry(alarmId) {
    if (!alarmId) return;

    for (let attempt = 1; attempt <= DELETE_RETRIES; attempt++) {
        const res = http.del(`${BASE_URL}/alarms/${alarmId}`, null, {
            headers: HEADERS,
            responseCallback: http.expectedStatuses(204, 404, 500),
        });

        if (res.status === 204 || res.status === 404) {
            return;
        }

        if (res.status === 500) {
            deleteRetries.add(1);
            console.warn(`SQLite lock on DELETE /alarms/${alarmId}, retry ${attempt}/${DELETE_RETRIES}`);
            sleep(DELETE_RETRY_DELAY);
            continue;
        }

        console.error(`Unexpected status on DELETE /alarms/${alarmId}: ${res.status}`);
        return;
    }

    console.error(`Failed to DELETE /alarms/${alarmId} after ${DELETE_RETRIES} retries`);
}

function isAlarmFiring(alarmId) {
    const res = http.get(`${BASE_URL}/alarms/${alarmId}`, { headers: HEADERS });
    if (res.status !== 200) {
        console.error(`Failed to get alarm ${alarmId}: ${res.status}`);
        return false;
    }
    const body = JSON.parse(res.body);
    const firing = body.status === 'FIRING';
    if (!firing) {
        console.warn(`Alarm ${alarmId} status is ${body.status}, expected FIRING`);
    }
    return firing;
}

function isHistoryFiring(alarmId) {
    const res = http.get(`${BASE_URL}/history/alarm/${alarmId}/latest`, {
        headers: HEADERS,
        responseCallback: http.expectedStatuses(200, 404),
    });

    if (res.status === 200) {
        const body = JSON.parse(res.body);
        return body.status === 'FIRING';
    }
    return false;
}

/* ============================================================
   SETUP / TEARDOWN
   ============================================================ */

export function setup() {
    console.log('=== Setup: criando alarmes no Broder ===');

    const alarmIds = [];

    // Thresholds baixos para garantir disparo mesmo com carga moderada
    alarmIds.push(createAlarm('stress_5xx', 'rate(http_server_requests_seconds_count{outcome="SERVER_ERROR"}[1m])', '0.01'));
    alarmIds.push(createAlarm('stress_4xx', 'rate(http_server_requests_seconds_count{outcome="CLIENT_ERROR"}[1m])', '0.01'));
    alarmIds.push(createAlarm('stress_cpu', 'process_cpu_usage', '0.1'));
    // Usa bytes absolutos para evitar dependência do heap max da JVM
    alarmIds.push(createAlarm('stress_mem', 'jvm_memory_used_bytes{area="heap"}', '50000000'));

    const validIds = alarmIds.filter(id => id !== null);
    console.log(`Alarmes criados: ${validIds.length}/4`);

    // Pré-alocar memória na ref app para elevar heap baseline
    console.log('=== Setup: pré-alocando memória na ref app ===');
    for (let i = 0; i < 6; i++) {
        const res = http.get(`${REF_APP_URL}/memory?megabytes=50`, { responseCallback: http.expectedStatuses(200) });
        if (res.status !== 200) {
            console.warn(`Falha ao pré-alocar memória: ${res.status}`);
        }
    }

    return { alarmIds: validIds };
}

export function teardown(data) {
    console.log('=== Teardown: limpando alarmes e memória ===');

    if (data && data.alarmIds) {
        data.alarmIds.forEach(id => deleteWithRetry(id));
    }

    http.get(`${REF_APP_URL}/memory/clear`, { responseCallback: http.expectedStatuses(200) });
}

/* ============================================================
   SCENARIOS
   ============================================================ */

export function stress5xx() {
    http.get(`${REF_APP_URL}/error-500`, { responseCallback: http.expectedStatuses(500) });
    http.get(`${REF_APP_URL}/hello`);
}

export function stress4xx() {
    http.get(`${REF_APP_URL}/error-400`, { responseCallback: http.expectedStatuses(400) });
    http.get(`${REF_APP_URL}/hello`);
}

export function stressCpu() {
    http.get(`${REF_APP_URL}/cpu?iterations=3000000`);
}

export function stressMemory() {
    http.get(`${REF_APP_URL}/memory?megabytes=20`, { responseCallback: http.expectedStatuses(200) });
    sleep(2);
}

export function verifyAlarms(data) {
    console.log('=== Verify: checando alarmes FIRING ===');

    if (!data.alarmIds || data.alarmIds.length === 0) {
        console.error('Nenhum alarme criado no setup');
        return;
    }

    // Aguardar mais um pouco para garantir que o scheduler avaliou após o stress contínuo
    sleep(WAIT_SECONDS);

    let firingCount = 0;
    let historyFiringCount = 0;

    data.alarmIds.forEach(id => {
        const firing = isAlarmFiring(id);
        if (firing) {
            firingCount++;
            console.log(`✅ Alarme ${id} está FIRING`);
        } else {
            console.error(`❌ Alarme ${id} NÃO está FIRING`);
        }

        const histFiring = isHistoryFiring(id);
        if (histFiring) {
            historyFiringCount++;
            console.log(`✅ Histórico do alarme ${id} registrou FIRING`);
        } else {
            console.warn(`⚠️ Histórico do alarme ${id} NÃO registrou FIRING`);
        }
    });

    alarmsFiringTotal.add(firingCount);
    alarmsCheckSuccess.add(firingCount / data.alarmIds.length);
    historyCheckSuccess.add(historyFiringCount / data.alarmIds.length);

    check(null, {
        'all alarms are FIRING': () => firingCount === data.alarmIds.length,
        'all histories recorded FIRING': () => historyFiringCount === data.alarmIds.length,
    });

    console.log(`Resultado: ${firingCount}/${data.alarmIds.length} alarmes FIRING, ${historyFiringCount}/${data.alarmIds.length} históricos FIRING`);
}
