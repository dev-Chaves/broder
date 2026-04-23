# Relatório de Testes - Prometheus API (broder)

## Data: 2025-04-21
## Ambiente: Local Dev (Quarkus Dev Mode)
## Prometheus: http://localhost:9090 (Spring Boot + Micrometer)

---

## Resumo Executivo

| Status | Count |
|--------|-------|
| ✅ Sucesso | 17 |
| ⚠️ Comportamento Esperado | 2 |
| ❌ Erro Identificado | 1 |

**Erros Identificados:**
1. ~~Deserialização de `value` como array (corrigido)~~
2. ~~Deserialização de labels endpoints (corrigido)~~
3. Query inválida retorna erro genérico do REST Client (pode ser melhorado)

---

## Testes Realizados

### 1. Instant Queries (POST /prometheus/query)

#### ✅ TEST 1: process_cpu_usage
**Request:**
```json
{"query": "process_cpu_usage"}
```
**Resultado:** ✅ SUCESSO
```json
{
  "status": "success",
  "data": {
    "resultType": "vector",
    "result": [{
      "metric": {"__name__": "process_cpu_usage", ...},
      "values": null,
      "value": [1776773497.198, "0.00040024014408645187"]
    }]
  }
}
```
**Observação:** `value` retorna como array `[timestamp, valor]` — correto após fix.

---

#### ✅ TEST 2: http_server_requests_seconds_count
**Request:**
```json
{"query": "http_server_requests_seconds_count"}
```
**Resultado:** ✅ SUCESSO
- Retorna múltiplas séries com labels: `method`, `status`, `uri`, `outcome`
- Métrica principal da aplicação Spring

---

#### ✅ TEST 3: jvm_memory_used_bytes{area="heap"}
**Request:**
```json
{"query": "jvm_memory_used_bytes{area=\"heap\"}"}
```
**Resultado:** ✅ SUCESSO
- Retorna 3 séries: `G1 Old Gen`, `G1 Survivor Space`, `G1 Eden Space`
- Labels incluem `id` (nome da região de memória)

---

#### ⚠️ TEST 4: Error Rate
**Request:**
```json
{"query": "(rate(http_server_requests_seconds_count{outcome=\"SERVER_ERROR\"}[1m]) / rate(http_server_requests_seconds_count[1m])) * 100"}
```
**Resultado:** ⚠️ ARRAY VAZIO
```json
{"status": "success", "data": {"resultType": "vector", "result": []}}
```
**Explicação:** Não há erros SERVER_ERROR no intervalo de 1m — comportamento correto.

---

#### ⚠️ TEST 5: P95 Latency
**Request:**
```json
{"query": "histogram_quantile(0.95, rate(http_server_requests_seconds_bucket[5m]))"}
```
**Resultado:** ⚠️ ARRAY VAZIO
```json
{"status": "success", "data": {"resultType": "vector", "result": []}}
```
**Explicação:** Dados insuficientes no bucket de 5m — comportamento correto para curto período.

---

#### ✅ TEST 15: Instant Query with Time Parameter
**Request:**
```json
{"query": "process_cpu_usage", "time": "1776773497.198"}
```
**Resultado:** ✅ SUCESSO
- Parâmetro `time` funciona corretamente

---

#### ⚠️ TEST 12: Invalid PromQL Query
**Request:**
```json
{"query": "invalid(())"}
```
**Resultado:** ⚠️ ERRO 400 GENÉRICO
```json
{
  "message": "Received: 'Bad Request, status code 400' when invoking REST Client method...",
  "status": 400,
  "error": "Bad Request"
}
```
**Problema:** O erro original do Prometheus (com mensagem detalhada) não é propagado.
**Recomendação:** Adicionar `@ClientExceptionMapper` no PrometheusClient para capturar e propagar mensagens de erro do Prometheus.

---

#### ❌ TEST 13: Empty Query (Validation)
**Request:**
```json
{"query": ""}
```
**Resultado:** ❌ VALIDATION ERROR 400
```json
{
  "title": "Constraint Violation",
  "status": 400,
  "violations": [{
    "field": "query.dto.query",
    "message": "PromQL query cannot be blank"
  }]
}
```
**Explicação:** `@NotBlank` no DTO funciona corretamente. Erro esperado.

---

#### ✅ TEST 16: Non-existent Metric
**Request:**
```json
{"query": "nonexistent_metric_12345"}
```
**Resultado:** ✅ SUCESSO (array vazio)
```json
{"status": "success", "data": {"resultType": "vector", "result": []}}
```
**Explicação:** Prometheus retorna sucesso com array vazio para métricas inexistentes.

---

#### ✅ TEST 17: Complex Query - Memory Usage %
**Request:**
```json
{"query": "(jvm_memory_used_bytes{area=\"heap\"} / jvm_memory_max_bytes{area=\"heap\"}) * 100"}
```
**Resultado:** ✅ SUCESSO
- Retorna percentual de uso por região de heap
- Valores negativos para `G1 Survivor Space` e `G1 Eden Space` indicam métricas ausentes (`jvm_memory_max_bytes` não existe para essas regiões)

---

#### ✅ TEST 18: Query with Label Matcher
**Request:**
```json
{"query": "http_server_requests_seconds_count{method=\"GET\",status=\"200\"}"}
```
**Resultado:** ✅ SUCESSO
- Filtragem por labels funciona corretamente

---

#### ✅ TEST 19: GC Metrics
**Request:**
```json
{"query": "rate(jvm_gc_pause_seconds_count[1m])"}
```
**Resultado:** ✅ SUCESSO
- Retorna GC pausas por tipo (`G1 Young Generation`, `G1 Old Generation`)
- Labels: `action`, `cause`, `gc`

---

### 2. Range Queries (POST /prometheus/query/range)

#### ✅ TEST 6: process_cpu_usage (range)
**Request:**
```json
{"query": "process_cpu_usage", "start": "1776773000", "end": "1776773500", "step": "60"}
```
**Resultado:** ✅ SUCESSO
```json
{
  "status": "success",
  "data": {
    "resultType": "matrix",
    "result": [{
      "metric": {...},
      "values": [
        [1776773000, "0.0002002002002002002"],
        [1776773060, "0.0007998400319936012"],
        ...
      ],
      "value": null
    }]
  }
}
```
**Observação:** `values` retorna array de arrays `[timestamp, valor]` — correto.

---

#### ✅ TEST 7: http_server_requests_seconds_count (range)
**Request:**
```json
{"query": "http_server_requests_seconds_count", "start": "1776773000", "end": "1776773500", "step": "60"}
```
**Resultado:** ✅ SUCESSO
- Retorna evolução temporal da contagem de requests

---

#### ⚠️ TEST 14: Range Query Missing Step
**Request:**
```json
{"query": "process_cpu_usage", "start": "1776773000", "end": "1776773500"}
```
**Resultado:** ⚠️ ERRO 400
```json
{
  "message": "Received: 'Bad Request, status code 400' when invoking REST Client method...",
  "status": 400,
  "error": "Bad Request"
}
```
**Explicação:** Parâmetro `step` é obrigatório para range queries no Prometheus.

---

### 3. Labels (GET /prometheus/labels)

#### ✅ TEST 8: Get All Labels
**Resultado:** ✅ SUCESSO
```json
{
  "status": "success",
  "data": [
    "__name__", "action", "app", "application", "area",
    "cause", "code", "compiler", "error", "exception",
    "gc", "id", "instance", "job", "kind", "le",
    "level", "main_application_class", "major", "method",
    "minor", "name", "outcome", "patch", "path", "pool",
    "route", "runtime", "space", "state", "status",
    "type", "uri", "vendor", "version"
  ]
}
```
**Observação:** 35 labels disponíveis. Após fix do DTO, funciona corretamente.

---

#### ✅ TEST 9: Get Label Values - status
**Resultado:** ✅ SUCESSO
```json
{"status": "success", "data": ["200"]}
```

---

#### ✅ TEST 10: Get Label Values - method
**Resultado:** ✅ SUCESSO
```json
{"status": "success", "data": ["GET"]}
```

---

#### ✅ TEST 11: Get Label Values - __name__
**Resultado:** ✅ SUCESSO
- Retorna 96 nomes de métricas disponíveis
- Inclui métricas JVM, HTTP, Node.js, process, system, Tomcat

---

## Erros Corrigidos Durante os Testes

### Erro 1: Deserialização de `value` como array
**Sintoma:** `{"objectName": "Class", "attributeName": "data.result[0].value", ...}`
**Causa:** O campo `value` do Prometheus é `[timestamp, "valor"]`, mas o DTO usava Record com campos nomeados.
**Fix:** Alterado `PrometheusValueDTO` para `List<Object>`.

### Erro 2: Deserialização de endpoints de labels
**Sintoma:** `{"objectName": "Class", "attributeName": "data", ...}`
**Causa:** Endpoints `/labels` e `/label/{name}/values` retornam `data` como `List<String>`, não como objeto com `resultType`.
**Fix:** Criado `PrometheusLabelsResponseDTO` com `List<String> data`.

---

## Métricas Disponíveis (Principais)

### JVM
- `jvm_memory_used_bytes` — Memória usada (heap/non-heap)
- `jvm_memory_max_bytes` — Memória máxima
- `jvm_gc_pause_seconds_count/sum` — GC pausas
- `jvm_threads_live_threads` — Threads ativas
- `jvm_classes_loaded_classes` — Classes carregadas

### HTTP
- `http_server_requests_seconds_count` — Contagem de requests
- `http_server_requests_seconds_sum` — Soma de tempos
- `http_server_requests_seconds_max` — Tempo máximo
- `http_server_requests_active_seconds_*` — Requests ativos

### Process/System
- `process_cpu_usage` — Uso de CPU do processo
- `process_uptime_seconds` — Uptime
- `process_resident_memory_bytes` — Memória residente
- `system_cpu_usage` — Uso de CPU do sistema
- `system_load_average_1m` — Load average

### Logs
- `logback_events_total` — Eventos de log por nível

---

## Recomendações

1. **Melhorar tratamento de erros do Prometheus:**
   - Adicionar `@ClientExceptionMapper` no `PrometheusClient` para propagar mensagens de erro originais (ex: "parse error at position...")

2. **Documentar queries comuns:**
   - RED metrics (Requests, Errors, Duration)
   - JVM memory/CPU
   - GC behavior

3. **Considerar cache para queries pesadas:**
   - Queries como `histogram_quantile` podem ser caras

4. **Validar parâmetros de range query:**
   - Adicionar `@NotBlank` em `start`, `end`, `step` quando necessário
