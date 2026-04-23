# Sistema de Logging - broder

## Visão Geral

Sistema de logging abrangente implementado em toda a aplicação usando **JBoss Logging** (padrão do Quarkus). Todos os logs são prefixados com `[TAG]` para facilitar filtragem e observabilidade.

---

## Componentes Logados

### 1. AlarmStartup `[STARTUP]`
- **INFO**: Início do processo de seed
- **INFO**: Conclusão do seed
- **ERROR**: Falhas no seed

**Exemplo:**
```
[STARTUP] Application starting up - checking alarm seed data
[STARTUP] Alarm seeding process completed successfully
```

---

### 2. AlarmScheduler `[SCHEDULER]`
- **INFO**: Início do ciclo de avaliação
- **INFO**: Quantidade de alarmes ativos
- **DEBUG**: Avaliação individual de cada alarme
- **ERROR**: Erros inesperados na avaliação
- **INFO**: Resumo do ciclo (duração, triggered, resolved, errors)

**Exemplo:**
```
[SCHEDULER] Starting alarm evaluation cycle
[SCHEDULER] Evaluating 5 active alarm(s)
[SCHEDULER] Cycle completed in 50 ms | alarms=5, triggered=1, resolved=0, errors=0
```

---

### 3. AlarmEvaluator `[EVALUATOR]`
- **DEBUG**: Início da avaliação (query, comparison, threshold)
- **DEBUG**: Resposta do Prometheus recebida
- **DEBUG**: Valor extraído e resultado da comparação
- **ERROR**: Falhas na query do Prometheus
- **WARN**: Parse errors de valores

**Exemplo:**
```
[EVALUATOR] Starting evaluation for alarm id=5, name='CPU Always Fires'
[EVALUATOR] Query: process_cpu_usage | Comparison: > | Threshold: 0.0001
[EVALUATOR] Condition: 0.000200 > 0.000100 => true
```

---

### 4. AlarmService `[SERVICE]`
- **INFO**: Criação de alarme
- **INFO**: Atualização de alarme
- **INFO**: Deleção de alarme
- **INFO**: Transição de status (ACTIVE → FIRING, FIRING → RESOLVED)
- **WARN**: Alarme não encontrado
- **DEBUG**: Listagem de alarmes
- **INFO**: Seed de alarmes padrão

**Exemplo:**
```
[SERVICE] Creating new alarm: name='CPU Always Fires', query='process_cpu_usage'
[SERVICE] Alarm created successfully: id=5
[SERVICE] Alarm id=5 transitioned to FIRING (previous: ACTIVE)
```

---

### 5. AlarmResource `[API]`
- **INFO**: Requisições HTTP recebidas (método, path, parâmetros)

**Exemplo:**
```
[API] GET /alarms - Listing all alarms
[API] POST /alarms - Creating alarm: name='CPU Always Fires'
[API] DELETE /alarms/5 - Deleting alarm
```

---

### 6. PrometheusService `[PROMETHEUS]`
- **DEBUG**: Queries enviadas ao Prometheus
- **DEBUG**: Respostas recebidas (status, resultType, count)
- **ERROR**: Falhas nas chamadas REST

**Exemplo:**
```
[PROMETHEUS] Instant query: process_cpu_usage
[PROMETHEUS] Instant query response: status=success, resultType=vector, resultCount=1
```

---

### 7. PrometheusResource `[API]`
- **INFO**: Requisições HTTP para endpoints do Prometheus

**Exemplo:**
```
[API] POST /prometheus/query - Query: process_cpu_usage
[API] GET /prometheus/labels - Querying all labels
```

---

### 8. AlarmNotificationService
- **INFO**: Alarme disparado (FIRING)
- **INFO**: Alarme resolvido (RESOLVED)
- **ERROR**: Erros na avaliação

**Exemplo:**
```
🚨 ALARM FIRING: CPU Always Fires (ID: 5) | Current: 0.0002 > Threshold: 0.0001
✅ ALARM RESOLVED: CPU Always Fires (ID: 5) | Current: 0.0000
```

---

## Níveis de Log

| Nível | Uso | Componentes |
|-------|-----|-------------|
| **ERROR** | Falhas críticas | Scheduler, Evaluator, Service, Startup |
| **WARN** | Problemas recuperáveis | Service (not found), Evaluator (parse) |
| **INFO** | Eventos principais | Todos os componentes |
| **DEBUG** | Detalhes operacionais | Scheduler, Evaluator, PrometheusService |
| **TRACE** | Detalhes mínimos | History recording |

---

## Filtragem de Logs

### Ver apenas o Scheduler
```bash
tail -f /tmp/quarkus-logs.txt | grep "\[SCHEDULER\]"
```

### Ver apenas alarmes disparados
```bash
tail -f /tmp/quarkus-logs.txt | grep "ALARM FIRING"
```

### Ver todas as chamadas API
```bash
tail -f /tmp/quarkus-logs.txt | grep "\[API\]"
```

### Ver erros do Prometheus
```bash
tail -f /tmp/quarkus-logs.txt | grep "\[PROMETHEUS\]" | grep ERROR
```

---

## Configuração de Níveis

Em `application.yaml`:
```yaml
quarkus:
  log:
    category:
      "org.acme.domain.alarm.AlarmScheduler":
        level: DEBUG
      "org.acme.domain.alarm.AlarmEvaluator":
        level: DEBUG
      "org.acme.domain.prometheus.PrometheusService":
        level: DEBUG
```

---

## Mapeamento de Observabilidade

| Evento | Onde é logado | Nível |
|--------|---------------|-------|
| Aplicação inicia | AlarmStartup | INFO |
| Alarme criado | AlarmService | INFO |
| Alarme atualizado | AlarmService | INFO |
| Alarme deletado | AlarmService | INFO |
| Ciclo scheduler inicia | AlarmScheduler | INFO |
| Alarme avaliado | AlarmEvaluator | DEBUG |
| Query Prometheus enviada | PrometheusService | DEBUG |
| Query Prometheus falhou | PrometheusService | ERROR |
| Alarme disparou | AlarmNotificationService | INFO |
| Alarme resolvido | AlarmNotificationService | INFO |
| Erro na avaliação | AlarmScheduler | ERROR |
| Status transicionou | AlarmService | INFO |
| API chamada | AlarmResource / PrometheusResource | INFO |
| Alarme não encontrado | AlarmService | WARN |
| Ciclo scheduler completo | AlarmScheduler | INFO |
| Valor inválido parseado | AlarmEvaluator | WARN |
| Seed de alarmes | AlarmService | INFO |

---

## Exemplo de Fluxo Completo no Log

```
[STARTUP] Application starting up - checking alarm seed data
[SERVICE] Seeding default alarms. Existing count: 0
[SERVICE] Default alarms seeded successfully. Total: 4
[STARTUP] Alarm seeding process completed successfully

[SCHEDULER] Starting alarm evaluation cycle
[SCHEDULER] Evaluating 5 active alarm(s)
[EVALUATOR] Starting evaluation for alarm id=5, name='CPU Always Fires'
[EVALUATOR] Query: process_cpu_usage | Comparison: > | Threshold: 0.0001
[PROMETHEUS] Instant query: process_cpu_usage
[PROMETHEUS] Instant query response: status=success, resultType=vector, resultCount=1
[EVALUATOR] Extracted value: 0.000200 from raw: 0.0002
[EVALUATOR] Condition: 0.000200 > 0.000100 => true
[SERVICE] Alarm id=5 transitioned to FIRING (previous: ACTIVE)
🚨 ALARM FIRING: CPU Always Fires (ID: 5) | Current: 0.0002 > Threshold: 0.0001
[SCHEDULER] Cycle completed in 50 ms | alarms=5, triggered=1, resolved=0, errors=0
```
