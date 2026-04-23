# Relatório: Alarm Builder API

## Data: 2025-04-21
## Status: ✅ COMPLETO

---

## O que foi implementado

### 1. `AlarmSeverity` Enum
- LOW, MEDIUM, HIGH, CRITICAL

### 2. `AlarmTemplate` Record
- id, name, description, category, defaultSeverity, defaultComparison, defaultThreshold, supportedFilters, unit, queryTemplate

### 3. `AlarmTemplateRegistry` (12 templates)

| ID | Nome | Categoria | Severidade | Query |
|----|------|-----------|------------|-------|
| cpu_usage_high | CPU Usage High | JVM | HIGH | process_cpu_usage |
| memory_heap_high | Memory Heap High | JVM | HIGH | jvm_memory_used_bytes{area="heap"} / jvm_memory_max_bytes{area="heap"} |
| http_error_rate | HTTP Error Rate (5xx) | HTTP | CRITICAL | rate(http_server_requests_seconds_count{outcome="SERVER_ERROR"}[1m]) |
| http_client_error_rate | HTTP Client Error Rate (4xx) | HTTP | MEDIUM | rate(http_server_requests_seconds_count{outcome="CLIENT_ERROR"}[1m]) |
| http_p50_latency | P50 Latency | HTTP | LOW | histogram_quantile(0.50, rate(...)) |
| http_p90_latency | P90 Latency | HTTP | MEDIUM | histogram_quantile(0.90, rate(...)) |
| http_p99_latency | P99 Latency | HTTP | HIGH | histogram_quantile(0.99, rate(...)) |
| http_avg_response_time | Avg Response Time | HTTP | MEDIUM | rate(sum) / rate(count) |
| http_rps | Requests Per Second | HTTP | LOW | rate(count) |
| gc_pause_p95 | GC Pause P95 | JVM | HIGH | histogram_quantile(0.95, rate(...)) |
| db_connections_high | DB Connections High | Database | HIGH | jdbc_connections_active |
| thread_count_high | Thread Count High | JVM | MEDIUM | jvm_threads_live |

### 4. `AlarmBuilderService`
- `listTemplates()` — lista todos os templates
- `listCategories()` — lista categorias únicas
- `listMetrics()` — lista métricas disponíveis
- `findTemplate(id)` — busca template por ID
- `buildQuery(templateId, filters)` — monta query PromQL com filtros dinâmicos
- `preview(dto)` — preview + execução no Prometheus (retorna valor atual e wouldTrigger)
- `validate(dto)` — valida sem executar query

### 5. `AlarmBuilderResource` — Novos Endpoints

| Method | Path | Descrição |
|--------|------|-----------|
| GET | `/alarms/builder/templates` | Lista templates |
| GET | `/alarms/builder/categories` | Lista categorias |
| GET | `/alarms/builder/metrics` | Lista métricas |
| POST | `/alarms/builder/preview` | Preview com execução no Prometheus |
| POST | `/alarms/builder/validate` | Validação sem execução |
| POST | `/alarms/builder` | Cria alarme via builder |

### 6. Modificações no Banco
- `Alarm` entity: +`severity`, +`category`, +`templateId`
- `AlarmRequestDTO`: +severity, +category, +templateId
- `AlarmResponseDTO`: +severity, +category, +templateId
- `AlarmUpdateDTO`: +severity

---

## Testes Realizados

### Test 1: List Templates
```bash
GET /alarms/builder/templates
```
✅ Retorna 12 templates com metadados completos

### Test 2: List Categories
```bash
GET /alarms/builder/categories
```
✅ Retorna: ["JVM", "HTTP", "Database"]

### Test 3: List Metrics
```bash
GET /alarms/builder/metrics
```
✅ Retorna 9 métricas com labels disponíveis

### Test 4: Preview
```bash
POST /alarms/builder/preview
{
  "templateId": "http_p99_latency",
  "filters": {"uri": "/api/orders", "method": "POST"},
  "comparison": ">",
  "threshold": "2.0",
  "severity": "CRITICAL"
}
```
✅ Retorna:
```json
{
  "generatedQuery": "histogram_quantile(0.99, rate(http_server_requests_seconds_bucket{uri=\"/api/orders\",method=\"POST\"}[1m]))",
  "currentValue": 0.0,
  "wouldTrigger": false,
  "severity": "CRITICAL",
  "category": "HTTP"
}
```

### Test 5: Build Alarm (CPU)
```bash
POST /alarms/builder
{
  "name": "CPU Alto",
  "templateId": "cpu_usage_high",
  "comparison": ">",
  "threshold": "0.5",
  "severity": "HIGH"
}
```
✅ Cria alarme com severity=HIGH, category=JVM, templateId=cpu_usage_high

### Test 6: Build Alarm (RPS)
```bash
POST /alarms/builder
{
  "name": "RPS Alto",
  "templateId": "http_rps",
  "filters": {"uri": "/api/orders"},
  "comparison": ">",
  "threshold": "100"
}
```
✅ Query: `rate(http_server_requests_seconds_count{uri="/api/orders"}[1m])`

### Test 7: Build Alarm (GC)
```bash
POST /alarms/builder
{
  "name": "GC Pause Alto",
  "templateId": "gc_pause_p95",
  "comparison": ">",
  "threshold": "0.3",
  "severity": "CRITICAL"
}
```
✅ Query: `histogram_quantile(0.95, rate(jvm_gc_pause_seconds_bucket[1m]))`

---

## Exemplo de Fluxo Completo

```
1. UI carrega templates:
   GET /alarms/builder/templates
   → 12 templates disponíveis

2. Usuário seleciona "P99 Latency":
   → category: HTTP, defaultSeverity: HIGH

3. Usuário define filtros:
   → uri: /api/checkout, method: POST

4. Preview (opcional):
   POST /alarms/builder/preview
   → Query gerada, valor atual, wouldTrigger

5. Cria alarme:
   POST /alarms/builder
   → Alarme salvo no SQLite com templateId, category, severity

6. Scheduler avalia a cada 20s
   → Executa query PromQL
   → Grava History com status e valor
   → Notifica se FIRING
```

---

## Arquivos Criados

- `AlarmSeverity.java`
- `AlarmTemplate.java`
- `AlarmTemplateRegistry.java`
- `AlarmBuilderRequestDTO.java`
- `AlarmPreviewResponseDTO.java`
- `MetricInfoDTO.java`
- `AlarmBuilderService.java`
- `AlarmBuilderResource.java`

## Arquivos Modificados

- `Alarm.java` (+severity, +category, +templateId)
- `AlarmRequestDTO.java` (+severity, +category, +templateId)
- `AlarmResponseDTO.java` (+severity, +category, +templateId)
- `AlarmUpdateDTO.java` (+severity)
