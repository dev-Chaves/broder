# Prometheus Queries Prontas para Uso

## 📊 Queries para Dashboard

### Seção 1: Taxa de Requisições

```promql
# RPS (Requisições Por Segundo)
rate(http.server.requests_seconds_count[1m])

# RPS Total
sum(rate(http.server.requests_seconds_count[1m]))

# RPS por Status
sum by (status) (rate(http.server.requests_seconds_count[1m]))

# RPS por Método HTTP
sum by (method) (rate(http.server.requests_seconds_count[1m]))

# RPS por Endpoint
sum by (uri) (rate(http.server.requests_seconds_count[1m]))
```

### Seção 2: Taxa de Erro

```promql
# Taxa de erro total (5xx)
rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[1m])

# Taxa de erro em percentual
(rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[1m]) / 
 rate(http.server.requests_seconds_count[1m])) * 100

# Erros por endpoint
sum by (uri) (rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[1m]))

# Taxa de erro de cliente (4xx)
rate(http.server.requests_seconds_count{outcome="CLIENT_ERROR"}[1m])

# Status 500 por segundo
rate(http.server.requests_seconds_count{status="500"}[1m])

# Status 503 por segundo
rate(http.server.requests_seconds_count{status="503"}[1m])
```

### Seção 3: Latência (Tempo de Resposta)

```promql
# P50 (Mediana)
histogram_quantile(0.50, rate(http.server.requests_seconds_bucket[5m]))

# P95
histogram_quantile(0.95, rate(http.server.requests_seconds_bucket[5m]))

# P99
histogram_quantile(0.99, rate(http.server.requests_seconds_bucket[5m]))

# P99.9
histogram_quantile(0.999, rate(http.server.requests_seconds_bucket[5m]))

# Latência média
(rate(http.server.requests_seconds_sum[5m]) / 
 rate(http.server.requests_seconds_count[5m]))

# Latência máxima (aproximado)
max(rate(http.server.requests_seconds_bucket[5m]))

# P95 por método HTTP
histogram_quantile(0.95, sum by (method, le) 
  (rate(http.server.requests_seconds_bucket[5m])))

# P95 por endpoint (TOP 10)
topk(10, histogram_quantile(0.95, sum by (uri, le) 
  (rate(http.server.requests_seconds_bucket[5m]))))
```

### Seção 4: Memória JVM

```promql
# Memória heap usada em bytes
jvm.memory.used{area="heap"}

# Memória heap máxima em bytes
jvm.memory.max{area="heap"}

# Percentual de heap usado
(jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"}) * 100

# Memória non-heap usada
jvm.memory.used{area="nonheap"}

# Tendência de uso de memória (últimas 5 min)
jvm.memory.used{area="heap"}

# Total de memória (heap + non-heap)
jvm.memory.used
```

### Seção 5: CPU e Sistema

```promql
# CPU processo (percentual)
process.cpu.usage * 100

# CPU sistema (percentual)
system.cpu.usage * 100

# Threads vivas
jvm.threads.live

# Threads pico
jvm.threads.peak

# Threads daemon
jvm.threads.daemon

# Memória livre sistema (em GB)
system.memory.free / 1073741824

# Memória total sistema (em GB)
system.memory.total / 1073741824

# Uso memória sistema (percentual)
(system.memory.total - system.memory.free) / system.memory.total * 100

# RSS do processo (em MB)
process.memory.rss / 1048576
```

### Seção 6: Garbage Collection

```promql
# Taxa de GC (eventos por segundo)
rate(jvm.gc.pause_seconds_count[1m])

# P95 tempo de pausa GC
histogram_quantile(0.95, rate(jvm.gc.pause_seconds_bucket[5m]))

# P99 tempo de pausa GC
histogram_quantile(0.99, rate(jvm.gc.pause_seconds_bucket[5m]))

# Percentual de tempo em GC
(rate(jvm.gc.pause_seconds_sum[5m]) / 300) * 100

# Tempo total alocado desde inicialização
jvm.gc.memory.allocated

# Tempo total promovido
jvm.gc.memory.promoted

# Taxa de alocação (bytes/segundo)
rate(jvm.gc.memory.allocated[1m])

# Taxa de promoção (bytes/segundo)
rate(jvm.gc.memory.promoted[1m])

# GC Young Generation
rate(jvm.gc.pause_seconds_count{gc="G1 Young Generation"}[1m])

# GC Old Generation
rate(jvm.gc.pause_seconds_count{gc="G1 Old Generation"}[1m])
```

### Seção 7: Database

```promql
# Conexões ativas
jdbc.connections.active

# Conexões ociosas
jdbc.connections.idle

# Conexões máximas
jdbc.connections.max

# Uso de conexões (percentual)
(jdbc.connections.active / jdbc.connections.max) * 100

# Conexões aguardando
jdbc.connections.pending

# Taxa de timeout conexão
rate(hikaricp.connections.timeout_total[5m])

# Total de timeouts
hikaricp.connections.timeout

# Entidades Hibernate carregadas por segundo
rate(hibernate.entities.loads[1m])

# Entidades Hibernate criadas por segundo
rate(hibernate.entities.creates[1m])

# Taxa de flush Hibernate (por segundo)
rate(hibernate.flush.time_seconds_count[1m])
```

### Seção 8: Cache

```promql
# Taxa de hit
rate(cache.gets.hit[5m])

# Taxa de miss
rate(cache.gets.miss[5m])

# Percentual de hit
(rate(cache.gets.hit[5m]) / rate(cache.gets[5m])) * 100

# Tamanho do cache
cache.size

# Taxa de evictions
rate(cache.evictions[5m])

# Taxa de puts
rate(cache.puts[5m])
```

### Seção 9: Logging

```promql
# Erros por segundo
rate(logback.events_total{level="ERROR"}[1m])

# Warnings por segundo
rate(logback.events_total{level="WARN"}[1m])

# Total de erros
logback.events_total{level="ERROR"}

# Total de warnings
logback.events_total{level="WARN"}

# Taxa de logs por nível
sum by (level) (rate(logback.events_total[5m]))
```

### Seção 10: Saúde Geral

```promql
# Uptime da aplicação (em horas)
process.uptime_seconds / 3600

# Uptime (sim/não)
process.uptime_seconds > 0

# Classes carregadas
jvm.classes.loaded

# Classes descarregadas
jvm.classes.unloaded

# Total de file descriptors
process.files.open

# File descriptors máximo
process.files.max

# Uso de file descriptors (percentual)
(process.files.open / process.files.max) * 100
```

---

## 🚨 Alertas Recomendados

### Críticos (Imediato - P1)

```yaml
groups:
- name: critical_alerts
  interval: 30s
  rules:
  
  - alert: HighErrorRate
    expr: |
      (sum(rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[5m])) /
       sum(rate(http.server.requests_seconds_count[5m]))) > 0.05
    for: 2m
    annotations:
      summary: "Taxa de erro acima de 5%"
      description: "{{ $value | humanizePercentage }} dos requests falhando"

  - alert: ServiceDown
    expr: process.uptime_seconds < 60
    for: 1m
    annotations:
      summary: "Serviço reiniciado ou down"
      description: "Uptime abaixo de 1 minuto"

  - alert: OutOfMemory
    expr: |
      (jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"}) > 0.95
    for: 1m
    annotations:
      summary: "Heap memory crítica (>95%)"
      description: "{{ $value | humanizePercentage }}"

  - alert: HighLatency
    expr: |
      histogram_quantile(0.99, rate(http.server.requests_seconds_bucket[5m])) > 5
    for: 5m
    annotations:
      summary: "P99 latência acima de 5 segundos"
      description: "{{ $value }}s de latência"
```

### Avisos (Ação Necessária - P2)

```yaml
groups:
- name: warning_alerts
  interval: 1m
  rules:
  
  - alert: HighMemoryUsage
    expr: |
      (jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"}) > 0.85
    for: 10m
    annotations:
      summary: "Heap memory elevada (>85%)"
      description: "{{ $value | humanizePercentage }}"

  - alert: HighGCPause
    expr: |
      histogram_quantile(0.95, rate(jvm.gc.pause_seconds_bucket[5m])) > 0.5
    for: 5m
    annotations:
      summary: "GC pause > 500ms"
      description: "P95 GC pause: {{ $value }}s"

  - alert: DBConnectionPoolAlmostFull
    expr: |
      (jdbc.connections.active / jdbc.connections.max) > 0.8
    for: 5m
    annotations:
      summary: "Pool de conexões DB > 80%"
      description: "{{ $value | humanizePercentage }} em uso"

  - alert: HighCPUUsage
    expr: process.cpu.usage > 0.8
    for: 10m
    annotations:
      summary: "CPU processo > 80%"
      description: "{{ $value | humanizePercentage }}"

  - alert: HighSystemMemory
    expr: |
      ((system.memory.total - system.memory.free) / system.memory.total) > 0.85
    for: 10m
    annotations:
      summary: "Memória sistema > 85%"
      description: "{{ $value | humanizePercentage }}"

  - alert: LowFileDescriptors
    expr: |
      ((process.files.max - process.files.open) / process.files.max) < 0.1
    for: 5m
    annotations:
      summary: "Espaço de FDs < 10%"
      description: "{{ $value | humanizePercentage }} disponível"

  - alert: CacheMissRate
    expr: |
      (rate(cache.gets.miss[5m]) / rate(cache.gets[5m])) > 0.5
    for: 5m
    annotations:
      summary: "Taxa de cache miss > 50%"
      description: "{{ $value | humanizePercentage }}"

  - alert: DBConnectionTimeouts
    expr: |
      rate(hikaricp.connections.timeout_total[5m]) > 0
    for: 5m
    annotations:
      summary: "Ocorrendo timeout de conexão DB"
      description: "{{ $value }} timeouts por segundo"
```

### Informativos (Monitorar - P3)

```yaml
groups:
- name: info_alerts
  interval: 5m
  rules:
  
  - alert: SlowEndpoints
    expr: |
      topk(5, histogram_quantile(0.95, sum by (uri, le) 
        (rate(http.server.requests_seconds_bucket[5m])))) > 1
    annotations:
      summary: "Endpoint lento (P95 > 1s)"
      description: "{{ $labels.uri }}: {{ $value }}s"

  - alert: HighLogErrorRate
    expr: |
      rate(logback.events_total{level="ERROR"}[5m]) > 1
    for: 10m
    annotations:
      summary: "Taxa de erros em log > 1/s"
      description: "{{ $value }} erros por segundo"

  - alert: UnusualGCFrequency
    expr: |
      rate(jvm.gc.pause_seconds_count[5m]) > 5
    for: 10m
    annotations:
      summary: "GC ocorrendo muito frequentemente (>5/s)"
      description: "{{ $value }} eventos GC por segundo"

  - alert: ThreadCount
    expr: jvm.threads.live > 500
    for: 10m
    annotations:
      summary: "Alto número de threads (>500)"
      description: "{{ $value }} threads vivas"
```

---

## 📈 Dashboard Completo (Prometheus/Grafana)

### Variáveis (Templating)

```
job_name:      label_values(http.server.requests_seconds_count, job)
application:   label_values(http.server.requests_seconds_count, application)
endpoint:      label_values(http.server.requests_seconds_count{job="$job_name"}, uri)
```

### Painéis Recomendados

#### 1. Overview
```
Linha: RPS Total = sum(rate(http.server.requests_seconds_count[1m]))
Linha: Taxa de Erro = (rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[1m]) / rate(http.server.requests_seconds_count[1m])) * 100
Gauge: P99 Latência = histogram_quantile(0.99, rate(http.server.requests_seconds_bucket[5m]))
Gauge: Uptime = process.uptime_seconds / 3600
```

#### 2. Performance
```
Gráfico: RPS por Status = sum by (status) (rate(http.server.requests_seconds_count[1m]))
Gráfico: Latência (P50, P95, P99) = múltiplas queries
Tabela: Top endpoints lentos = topk(10, histogram_quantile(0.95, ...))
```

#### 3. Recursos
```
Gauge: CPU = process.cpu.usage * 100
Gauge: Heap % = (jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"}) * 100
Gauge: Memória Sistema % = ((system.memory.total - system.memory.free) / system.memory.total) * 100
Gauge: DB Connections % = (jdbc.connections.active / jdbc.connections.max) * 100
```

#### 4. JVM
```
Gráfico: Memória = jvm.memory.used{area="heap"}
Gráfico: GC Pause = histogram_quantile(0.95, rate(jvm.gc.pause_seconds_bucket[5m]))
Gráfico: Threads = jvm.threads.live
Gráfico: GC Events = rate(jvm.gc.pause_seconds_count[1m])
```

#### 5. Database
```
Gauge: Conexões em Uso % = jdbc.connections.usage
Série: Conexões = jdbc.connections.active
Tabela: Hibernate Stats = hibernate.entities.loads, creates, updates, deletes
```

---

## 💡 Dicas de Otimização

### Performance da Query

```promql
# ❌ RUIM - Muito cara
histogram_quantile(0.95, http.server.requests_seconds_bucket)

# ✅ BOM - Com rate e range
histogram_quantile(0.95, rate(http.server.requests_seconds_bucket[5m]))

# ❌ RUIM - Sem aggregation
http.server.requests_seconds_count

# ✅ BOM - Aggregado
sum(rate(http.server.requests_seconds_count[1m]))
```

### Reduzir Cardinality

```promql
# ❌ RUIM - Cada URI é uma série
http.server.requests_seconds_count by (uri)

# ✅ BOM - Agrupar URIs similares
http.server.requests_seconds_count by (uri =~ "/api/users.*")
```

---

## 🔍 Debugging

### Verificar métricas disponíveis

```
# No Prometheus UI: http://prometheus:9090/graph
# Buscar por:
- http_server_requests
- jvm_memory
- jdbc_connections
```

### Verificar labels de uma métrica

```promql
# Ver todos os labels disponíveis
label_names(http.server_requests_seconds_count)

# Ver valores de um label específico
label_values(http.server_requests_seconds_count, status)
```

### Testar query antes de usar em alerta

```promql
# Executar e ver resultado
http.server_requests_seconds_count > 0

# Verificar tipo
(http.server_requests_seconds_count > 0) and on() vector(1)
```

