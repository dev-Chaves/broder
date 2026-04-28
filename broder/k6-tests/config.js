/**
 * Configurações compartilhadas para os testes k6 do broder.
 */

export const BASE_URL = __ENV.BRODER_BASE_URL || 'http://localhost:8080';

export const HEADERS = {
    'Content-Type': 'application/json',
    'Accept': 'application/json',
};

export const DEFAULT_THRESHOLDS = {
    http_req_duration: ['p(95)<500'],
    http_req_failed: ['rate<0.05'],
};

/**
 * Gera um nome único para alarmes criados nos testes,
 * evitando conflitos em execuções paralelas ou repetidas.
 */
export function generateUniqueName(prefix) {
    return `${prefix}_${Math.random().toString(36).substring(2, 10)}_${Date.now()}`;
}
