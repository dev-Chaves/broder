# Métricas Default do Micrometer + Spring Boot

## 📋 Tabela de Conteúdo
1. [JVM Metrics](#jvm-metrics)
2. [System Metrics](#system-metrics)
3. [HTTP Server Requests](#http-server-requests)
4. [Application Metrics](#application-metrics)
5. [Database Metrics](#database-metrics)
6. [Cache Metrics](#cache-metrics)
7. [Logging Metrics](#logging-metrics)
8. [Scheduled Tasks](#scheduled-tasks)
9. [Exemplos de Queries Prometheus](#exemplos-de-queries-prometheus)
10. [Referências Oficiais](#referências-oficiais)

---

## JVM Metrics

### Prefixo: `jvm.*`

Todas as métricas JVM são coletadas automaticamente quando Micrometer está ativo.

### Memory Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `jvm.memory.used` | Gauge | Memória JVM usada (em bytes) |
| `jvm.memory.max` | Gauge | Memória JVM máxima (em bytes) |
| `jvm.memory.committed` | Gauge | Memória JVM committed (em bytes) |
| `jvm.memory.init` | Gauge | Memória JVM initial (em bytes) |

**Tags disponíveis:**
- `area`: "heap" ou "nonheap"
- `id`: nome do memory pool (ex: "G1 Eden Space", "CodeCache")

**Exemplo de Query Prometheus:**
```
# Memória heap usada (em MB)
jvm.memory.used{area="heap"} / 1048576

# Percentual de memória heap usada
100 * (jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"})

# Total de memória em todos os pools
jvm.memory.max{id="G1 Old Generation"}
```

### Garbage Collection Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `jvm.gc.memory.allocated` | Counter | Total de memória alocada desde inicialização |
| `jvm.gc.memory.promoted` | Counter | Total de memória promovida para heap antigo |
| `jvm.gc.max.data.size` | Gauge | Tamanho máximo de old generation |
| `jvm.gc.live.data.size` | Gauge | Tamanho live de old generation após full GC |
| `jvm.gc.pause` | Timer | Duração das pausas GC (histograma) |

**Tags disponíveis:**
- `gc`: nome do coletor GC (ex: "G1 Young Generation", "G1 Old Generation")
- `action`: ação do GC

**Exemplo:**
```
# Taxa de garbage collection (eventos por segundo)
rate(jvm.gc.pause_seconds_count[1m])

# Percentual de tempo em pausa GC
100 * rate(jvm.gc.pause_seconds_sum[5m]) / 300

# P95 da pausa GC
histogram_quantile(0.95, rate(jvm.gc.pause_seconds_bucket[5m]))
```

### Thread Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `jvm.threads.peak` | Gauge | Pico de threads |
| `jvm.threads.live` | Gauge | Threads vivas (ativas) |
| `jvm.threads.daemon` | Gauge | Threads daemon |
| `jvm.threads.started` | Counter | Total de threads iniciadas |
| `jvm.threads.states` | Gauge | Threads por estado |

**Tags disponíveis:**
- `state`: "new", "runnable", "blocked", "waiting", "timed-waiting", "terminated"

**Exemplo:**
```
# Threads bloqueadas
jvm.threads.states{state="blocked"}

# Taxa de novas threads criadas
rate(jvm.threads.started_total[1m])

# Threads em waiting (possível deadlock)
jvm.threads.states{state="waiting"}
```

### Class Loading Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `jvm.classes.loaded` | Gauge | Classes carregadas |
| `jvm.classes.unloaded` | Counter | Classes descarregadas |

---

## System Metrics

### Prefixo: `system.*`, `process.*`, `disk.*`

### CPU Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `system.cpu.usage` | Gauge | CPU usada pelo sistema (0-1) |
| `system.cpu.count` | Gauge | Número de CPUs disponíveis |
| `process.cpu.usage` | Gauge | CPU usada pela JVM (0-1) |

**Exemplo:**
```
# CPU do processo como percentual
100 * process.cpu.usage

# CPU do sistema como percentual
100 * system.cpu.usage
```

### File Descriptors

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `process.files.open` | Gauge | File descriptors abertos |
| `process.files.max` | Gauge | Limite máximo de file descriptors |

**Exemplo:**
```
# Percentual de file descriptors usados
100 * (process.files.open / process.files.max)
```

### Process & System Memory

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `process.memory.rss` | Gauge | Resident set size (memória física) |
| `process.memory.vms` | Gauge | Virtual memory size |
| `system.memory.total` | Gauge | Memória total do sistema |
| `system.memory.free` | Gauge | Memória livre do sistema |
| `system.memory.usage` | Gauge | Memória usada (0-1) |

**Exemplo:**
```
# Uso de memória física do processo (em MB)
process.memory.rss / 1048576

# Percentual de memória livre no sistema
100 * system.memory.free / system.memory.total

# Pressão de memória do sistema
100 * system.memory.usage
```

### Disk Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `disk.free` | Gauge | Espaço livre em disco |
| `disk.total` | Gauge | Espaço total em disco |
| `disk.usage` | Gauge | Espaço usado em disco |

**Tags:**
- `path`: caminho do disco

**Exemplo:**
```
# Percentual de disco usado
100 * (disk.usage / disk.total)
```

### Uptime Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `process.uptime` | Gauge | Tempo de execução da aplicação (segundos) |
| `process.cpu.time` | Gauge | Tempo de CPU usado (nanossegundos) |

---

## HTTP Server Requests

### Prefixo: `http.server.requests`

A instrumentação HTTP é habilitada automaticamente para requisições ao servidor.

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `http.server.requests` | Timer | Duração e contagem de requisições HTTP |

**Tags disponíveis:**
- `method`: GET, POST, PUT, DELETE, PATCH, etc.
- `uri`: endpoint (ex: "/api/users", "/metrics")
- `status`: código HTTP (200, 404, 500, etc.)
- `exception`: nome da exceção lançada (se houver)
- `outcome`: "SUCCESS", "CLIENT_ERROR", "SERVER_ERROR", "REDIRECT"

**Variações da métrica (histograma):**
```
http.server.requests_seconds_count    # Total de requisições
http.server.requests_seconds_sum      # Tempo total gasto
http.server.requests_seconds_bucket   # Histogram buckets (para percentis)
```

**Exemplos de Queries:**

```promql
# Total de requisições por segundo
rate(http.server.requests_seconds_count[1m])

# Taxa de erro (4xx + 5xx)
rate(http.server.requests_seconds_count{outcome!="SUCCESS"}[1m])

# P95 de latência (em segundos)
histogram_quantile(0.95, rate(http.server.requests_seconds_bucket[5m]))

# Requisições por endpoint
sum by (uri) (rate(http.server.requests_seconds_count[1m]))

# Status 500 por segundo
rate(http.server.requests_seconds_count{status="500"}[1m])

# Latência média por método HTTP
rate(http.server.requests_seconds_sum[5m]) / rate(http.server.requests_seconds_count[5m])

# Taxa de timeout (se aplicável)
rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[1m])
```

---

## Application Metrics

### Startup Metrics

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `application.started.time` | Timer | Tempo de inicialização da app |
| `application.ready.time` | Timer | Tempo até a app estar pronta |

### Custom Application Tags

Se tiver `spring.application.name` configurado, essa tag será adicionada a todas as métricas:

```properties
spring.application.name=meu-servico
```

Você verá uma tag `application="meu-servico"` em todas as métricas.

---

## Database Metrics

### JDBC Connection Pool

### Prefixo: `jdbc.connections`

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `jdbc.connections.active` | Gauge | Conexões ativas em uso |
| `jdbc.connections.idle` | Gauge | Conexões ociosas no pool |
| `jdbc.connections.max` | Gauge | Número máximo de conexões |
| `jdbc.connections.min` | Gauge | Número mínimo de conexões |
| `jdbc.connections.pending` | Gauge | Conexões aguardando obtenção |
| `jdbc.connections.usage` | Gauge | Percentual de conexões em uso |

**Tags:**
- `name`: nome da datasource (ex: "hikariPool")

**Exemplo:**
```promql
# Percentual de conexões em uso
jdbc.connections.usage{name="hikariPool"}

# Conexões ativas
jdbc.connections.active{name="hikariPool"}

# Conexões max atingindo limite?
rate(jdbc.connections.pending[5m]) > 0
```

### HikariCP Metrics

### Prefixo: `hikaricp`

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `hikaricp.connections` | Gauge | Total de conexões |
| `hikaricp.connections.active` | Gauge | Conexões ativas |
| `hikaricp.connections.idle` | Gauge | Conexões ociosas |
| `hikaricp.connections.pending` | Gauge | Conexões aguardando |
| `hikaricp.connections.timeout` | Counter | Timeouts de conexão |
| `hikaricp.idle.timeout` | Counter | Timeouts de idle |
| `hikaricp.max.lifetime` | Counter | Exceções de max lifetime |

**Exemplo:**
```promql
# Taxa de timeouts
rate(hikaricp.connections.timeout_total[5m])
```

### Hibernate Metrics

### Prefixo: `hibernate`

Requer: `org.hibernate.orm:hibernate-micrometer`

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `hibernate.entities.creates` | Counter | Entities criadas |
| `hibernate.entities.updates` | Counter | Entities atualizadas |
| `hibernate.entities.deletes` | Counter | Entities deletadas |
| `hibernate.entities.loads` | Counter | Entities carregadas |
| `hibernate.sessions.open` | Gauge | Sessões abertas |
| `hibernate.sessions.closed` | Counter | Sessões fechadas |
| `hibernate.cache.gets` | Counter | Cache hits + misses |
| `hibernate.cache.puts` | Counter | Cache puts |
| `hibernate.cache.evictions` | Counter | Cache evictions |
| `hibernate.flush.time` | Timer | Tempo de flush |
| `hibernate.query.execution.time` | Timer | Tempo de execução de queries |

**Tags:**
- `entityManagerFactory`: nome do EMF

---

## Cache Metrics

### Suportados: Guava, EhCache, Hazelcast, Caffeine, Coherence

### Prefixo: `cache`

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `cache.gets` | Counter | Total de gets (hits + misses) |
| `cache.gets.hit` | Counter | Cache hits |
| `cache.gets.miss` | Counter | Cache misses |
| `cache.puts` | Counter | Cache puts |
| `cache.evictions` | Counter | Evictions |
| `cache.removals` | Counter | Removals |
| `cache.size` | Gauge | Tamanho do cache |

**Tags:**
- `cache`: nome do cache
- `cachemanager`: nome do manager

**Exemplo:**
```promql
# Taxa de hit
rate(cache.gets.hit[5m]) / rate(cache.gets[5m])

# Taxa de miss
rate(cache.gets.miss[5m]) / rate(cache.gets[5m])
```

---

## Logging Metrics

### Logback

### Prefixo: `logback.events`

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `logback.events` | Counter | Total de eventos de log |

**Tags:**
- `level`: ERROR, WARN, INFO, DEBUG, TRACE

**Exemplo:**
```promql
# Taxa de erros
rate(logback.events_total{level="ERROR"}[5m])

# Taxa de warnings
rate(logback.events_total{level="WARN"}[5m])
```

### Log4J2

### Prefixo: `log4j2.events`

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `log4j2.events` | Counter | Total de eventos de log |

**Tags:**
- `level`: ERROR, WARN, INFO, DEBUG, TRACE

---

## Scheduled Tasks

### Executor Service Metrics

Instrumentação automática para `ThreadPoolTaskExecutor` e `ThreadPoolTaskScheduler`.

### Prefixo: `executor`

| Métrica | Tipo | Descrição |
|---------|------|-----------|
| `executor.active` | Gauge | Threads ativas |
| `executor.completed` | Counter | Tasks completadas |
| `executor.pool.size` | Gauge | Tamanho atual do pool |
| `executor.pool.core` | Gauge | Core pool size |
| `executor.pool.max` | Gauge | Max pool size |
| `executor.queued` | Gauge | Tasks na fila |
| `executor.queue.remaining` | Gauge | Espaço restante na fila |
| `executor.tasks.submitted` | Counter | Total de tasks submetidas |

**Tags:**
- `name`: nome do executor

**Exemplo:**
```promql
# Tamanho da fila
executor.queued{name="taskExecutor"}

# Taxa de rejeição de tasks
rate(executor.tasks.submitted[5m]) - rate(executor.completed[5m])
```

---

## Exemplos de Queries Prometheus

### 1. Dashboard Geral

```promql
# Taxa de requisições por segundo
rate(http.server.requests_seconds_count[1m])

# Taxa de erro em %
(rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[1m]) / rate(http.server.requests_seconds_count[1m])) * 100

# P95 latência
histogram_quantile(0.95, rate(http.server.requests_seconds_bucket[5m]))

# CPU uso
100 * process.cpu.usage

# Memória heap usada em %
100 * (jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"})

# Conexões DB em uso
jdbc.connections.usage
```

### 2. Performance Analysis

```promql
# Top 10 endpoints mais lentos (P99)
topk(10, histogram_quantile(0.99, rate(http.server.requests_seconds_bucket[5m])))

# Endpoints com maior volume
topk(10, rate(http.server.requests_seconds_count[5m]))

# Endpoints com maior taxa de erro
topk(10, rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[5m]))
```

### 3. Health Check

```promql
# Aplicação está UP? (uptime > 0)
process.uptime_seconds > 0

# GC pressure (pausas > 50ms)
histogram_quantile(0.95, rate(jvm.gc.pause_seconds_bucket[5m])) > 0.05

# Memory pressure
(jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"}) > 0.85

# DB connection pressure
jdbc.connections.usage > 0.8
```

### 4. Alertas Recomendados

```yaml
- alert: HighErrorRate
  expr: (rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[5m]) / rate(http.server.requests_seconds_count[5m])) > 0.05
  for: 5m
  annotations:
    summary: "Error rate > 5%"

- alert: HighLatency
  expr: histogram_quantile(0.99, rate(http.server.requests_seconds_bucket[5m])) > 1
  for: 5m
  annotations:
    summary: "P99 latency > 1s"

- alert: HighMemoryUsage
  expr: (jvm.memory.used{area="heap"} / jvm.memory.max{area="heap"}) > 0.9
  for: 5m
  annotations:
    summary: "Heap memory > 90%"

- alert: HighGCPause
  expr: histogram_quantile(0.95, rate(jvm.gc.pause_seconds_bucket[5m])) > 0.1
  for: 5m
  annotations:
    summary: "GC pause > 100ms"

- alert: DBConnectionPoolFull
  expr: jdbc.connections.usage > 0.9
  for: 5m
  annotations:
    summary: "DB connection pool > 90%"
```

---

## Configuração Minimal no `application.yaml`

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
  metrics:
    export:
      prometheus:
        enabled: true
    tags:
      application: ${spring.application.name}
      environment: ${app.environment:local}
      version: ${app.version:unknown}

spring:
  application:
    name: meu-servico
```

### Endpoints Disponíveis

```
GET /actuator/health           # Health status
GET /actuator/info             # App info
GET /actuator/metrics          # Lista de métricas
GET /actuator/metrics/{name}   # Detalhes de uma métrica
GET /actuator/prometheus       # Formato Prometheus (scrape)
```

---

## Referências Oficiais

### Spring Boot Documentation
- **Metrics:** https://docs.spring.io/spring-boot/reference/actuator/metrics.html
- **Supported Metrics:** https://rwinch.github.io/spring-boot/actuator/metrics/supported.html
- **Production Ready:** https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html

### Micrometer Official
- **Documentation:** https://micrometer.io/docs
- **GitHub:** https://github.com/micrometer-metrics/micrometer
- **Prometheus Support:** https://micrometer.io/docs/registry/prometheus

### Prometheus
- **Query Language:** https://prometheus.io/docs/prometheus/latest/querying/basics/
- **Functions:** https://prometheus.io/docs/prometheus/latest/querying/functions/
- **Best Practices:** https://prometheus.io/docs/practices/naming/

### Useful Links
- **Spring Boot Actuator Guide:** https://docs.spring.io/spring-boot/guides/gs-actuator-service/
- **Micrometer Concepts:** https://micrometer.io/docs/concepts
- **Histogram Quantiles:** https://prometheus.io/docs/prometheus/latest/querying/functions/#histogram_quantile

---

## Resumo Rápido

### Métrica mais importante para cada aspecto:

| Aspecto | Métrica | Crítica? |
|---------|---------|----------|
| Taxa de requisições | `http.server.requests_seconds_count` | ⭐⭐⭐ |
| Latência | `http.server.requests_seconds_bucket` | ⭐⭐⭐ |
| Erros | `http.server.requests_seconds_count{outcome="SERVER_ERROR"}` | ⭐⭐⭐ |
| Memória | `jvm.memory.used / jvm.memory.max` | ⭐⭐ |
| CPU | `process.cpu.usage` | ⭐⭐ |
| GC | `jvm.gc.pause_seconds_bucket` | ⭐⭐ |
| DB Connections | `jdbc.connections.usage` | ⭐⭐ |
| Uptime | `process.uptime_seconds` | ⭐ |

---

## Dicas Práticas

✅ **DO's:**
- Expor `/actuator/prometheus` apenas para Prometheus
- Adicionar tags customizadas (`application`, `environment`, `version`)
- Usar `histogram_quantile` para P95/P99 latência
- Monitorar GC pauses em aplicações críticas
- Alertar em > 80% de uso de recursos

❌ **DON'Ts:**
- Não expor todos os endpoints (`/actuator`) publicamente
- Não coletar métricas desnecessárias (overhead)
- Não ignorar warnings de memória/GC
- Não usar `time series cardinality` excessivamente
- Não fazer alertas muito sensíveis (geram false positives)

