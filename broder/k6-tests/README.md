# Testes de Carga k6 — broder

Este diretório contém o teste de carga principal do `broder`, focado em **validar o disparo de alarmes críticos** sob condições de stress na aplicação monitorada.

## Pré-requisitos

- [k6](https://k6.io/docs/get-started/installation/) instalado na máquina.
- O backend do `broder` rodando (porta padrão `8080`).
- A aplicação de referência (`api-prometheus/ref`) rodando e acessível.
- Prometheus acessível e scrapegando a ref app.

## Subir a stack completa

```bash
# 1. Subir Prometheus + ref app
cd api-prometheus && docker compose up -d

# 2. Subir o Broder (em outro terminal)
cd broder && ./mvnw quarkus:dev
```

## Estrutura

```
k6-tests/
├── config.js              # Configurações compartilhadas (URL, headers, utilitários)
└── alarm-stress-test.js   # Teste de carga para disparo de alarmes 5xx/4xx/CPU/Memory
```

## Execução

### Teste de Stress dos Alarmes

```bash
k6 run alarm-stress-test.js
```

Apontando para outro host:

```bash
k6 run --env BRODER_BASE_URL=http://meu-servidor:8080 --env REF_APP_URL=http://meu-servidor:8181 alarm-stress-test.js
```

## Cenários Cobertos (`alarm-stress-test.js`)

O teste executa em 5 cenários paralelos/sequenciais:

1. **`stress_5xx`** — 5 VUs por 150s batendo em `/error-500` e `/hello` da ref app, forçando taxa de erro 5xx.
2. **`stress_4xx`** — 5 VUs por 150s batendo em `/error-400` e `/hello`, forçando taxa de erro 4xx.
3. **`stress_cpu`** — 10 VUs por 150s batendo em `/cpu?iterations=3000000`, elevando `process_cpu_usage`.
4. **`stress_memory`** — 2 VUs por 150s batendo em `/memory?megabytes=20`, elevando `jvm_memory_used_bytes`.
5. **`verify`** — Após 120s de stress, 1 VU verifica se os 4 alarmes criados no `setup` estão com status **`FIRING`** e se o histórico registrou o disparo.

### Alarmes criados no Setup

| Alarme | Query | Threshold |
|--------|-------|-----------|
| 5xx Error Rate | `rate(http_server_requests_seconds_count{outcome="SERVER_ERROR"}[1m])` | `0.01` |
| 4xx Error Rate | `rate(http_server_requests_seconds_count{outcome="CLIENT_ERROR"}[1m])` | `0.01` |
| CPU Usage | `process_cpu_usage` | `0.1` |
| Memory Heap | `jvm_memory_used_bytes{area="heap"}` | `50000000` (50 MB) |

> Os thresholds são baixos para garantir disparo mesmo com carga moderada.

### Métricas customizadas no dashboard k6

- `alarms_firing_total` — total de alarmes que dispararam
- `alarms_check_success` — taxa de sucesso na verificação de status FIRING
- `history_check_success` — taxa de sucesso no histórico FIRING

## Thresholds

- **http_req_duration**: 95% das requisições devem ser menores que **3000ms**
- **http_req_failed**: taxa de falha menor que **15%**
- **alarms_check_success**: taxa de sucesso maior que **80%**
- **history_check_success**: taxa de sucesso maior que **80%**

## Limpeza de Dados

O teste remove automaticamente os alarmes criados no `teardown` e chama `/memory/clear` na ref app para liberar memória alocada.

## Endpoints de Stress da Ref App

A aplicação de referência (`api-prometheus`) possui endpoints dedicados para stress:

- `GET /error-500` — retorna HTTP 500
- `GET /error-400` — retorna HTTP 400
- `GET /cpu?iterations=N` — loop de cálculo intensivo
- `GET /memory?megabytes=N` — aloca N MB de heap
- `GET /memory/clear` — libera memória alocada e chama GC

## Variáveis de Ambiente

| Variável              | Padrão                  | Descrição                                        |
|-----------------------|-------------------------|--------------------------------------------------|
| `BRODER_BASE_URL`     | `http://localhost:8080` | URL base da API do broder                        |
| `REF_APP_URL`         | `http://localhost:8181` | URL base da ref app (api-prometheus)             |
| `WAIT_SECONDS`        | `25`                    | Tempo de espera adicional pelo scheduler         |
| `DELETE_RETRIES`      | `3`                     | Tentativas de DELETE com retry (SQLite lock)     |
| `DELETE_RETRY_DELAY`  | `1`                     | Delay em segundos entre retries de DELETE        |
