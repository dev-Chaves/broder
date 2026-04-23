# 📚 Referências Oficiais - Prometheus + Micrometer

## 🔗 Links Diretos para Documentação

### Spring Boot + Micrometer
- **Metrics (Spring Boot 3.4 - Atual):** https://docs.spring.io/spring-boot/reference/actuator/metrics.html
- **Metrics (Spring Boot 2.x):** https://docs.spring.io/spring-boot/docs/2.7.x/reference/html/production-ready-features.html
- **Supported Metrics:** https://rwinch.github.io/spring-boot/actuator/metrics/supported.html
- **Actuator Guide:** https://docs.spring.io/spring-boot/guides/gs-actuator-service/

### Micrometer Oficial
- **Homepage:** https://micrometer.io/
- **Documentação:** https://micrometer.io/docs
- **Prometheus Registry:** https://micrometer.io/docs/registry/prometheus
- **GitHub Repository:** https://github.com/micrometer-metrics/micrometer
- **GitHub Discussions:** https://github.com/micrometer-metrics/micrometer/discussions
- **Concepts & Types:** https://micrometer.io/docs/concepts

### Prometheus
- **Homepage:** https://prometheus.io/
- **Documentação:** https://prometheus.io/docs/
- **Query Language (PromQL):** https://prometheus.io/docs/prometheus/latest/querying/basics/
- **Functions:** https://prometheus.io/docs/prometheus/latest/querying/functions/
- **Operators:** https://prometheus.io/docs/prometheus/latest/querying/operators/
- **Best Practices:** https://prometheus.io/docs/practices/

---

## 📖 Tabelas Rápidas (Cheat Sheets)

### JVM Metrics - Nomes Completos

```
jvm.memory.used
jvm.memory.max
jvm.memory.committed
jvm.memory.init

jvm.gc.pause
jvm.gc.memory.allocated
jvm.gc.memory.promoted
jvm.gc.max.data.size
jvm.gc.live.data.size

jvm.threads.peak
jvm.threads.live
jvm.threads.daemon
jvm.threads.started
jvm.threads.states

jvm.classes.loaded
jvm.classes.unloaded
```

### System Metrics - Nomes Completos

```
system.cpu.usage
system.cpu.count
process.cpu.usage
process.cpu.time

process.files.open
process.files.max

process.memory.rss
process.memory.vms
system.memory.total
system.memory.free
system.memory.usage

disk.free
disk.total
disk.usage

process.uptime
```

### HTTP Metrics - Nomes Completos

```
http.server.requests         (Timer - múltiplas variações)
http.server.requests_seconds_count
http.server.requests_seconds_sum
http.server.requests_seconds_bucket
```

### Tags Padrão

| Métrica | Tags Disponíveis |
|---------|------------------|
| `jvm.memory.*` | `area` (heap, nonheap), `id` (memory pool) |
| `jvm.gc.pause` | `gc` (GC name), `action` |
| `jvm.threads.states` | `state` (runnable, blocked, waiting, etc) |
| `http.server.requests` | `method`, `uri`, `status`, `outcome`, `exception` |
| `jdbc.connections.*` | `name` (datasource name) |
| `hikaricp.*` | `pool` name |
| `cache.*` | `cache`, `cachemanager` |
| `logback.events` | `level` (ERROR, WARN, INFO, DEBUG, TRACE) |
| `hibernate.*` | `entityManagerFactory` |
| `executor.*` | `name` (executor name) |

---

## 🔑 Tags Padrão em Todas as Métricas

```yaml
# Se configurado:
application: ${spring.application.name}
environment: ${app.environment}
version: ${app.version}
instance: ${HOSTNAME}
job: ${prometheus.job.name}
```

---

## PromQL - Operadores e Funções Essenciais

### Operadores Aritméticos
```
+ (adição)
- (subtração)
* (multiplicação)
/ (divisão)
% (modulo)
^ (potência)
```

### Operadores de Comparação
```
== (igual)
!= (não igual)
> (maior)
< (menor)
>= (maior ou igual)
<= (menor ou igual)
```

### Operadores Lógicos
```
and    (intersecção)
or     (união)
unless (subtração)
```

### Funções de Agregação
```
sum()       - Soma valores
avg()       - Média
min()       - Mínimo
max()       - Máximo
count()     - Conta elementos
stddev()    - Desvio padrão
group()     - Agrupa (apenas nome)
topk(n)     - Top N
bottomk(n)  - Bottom N
```

### Funções de Rate/Increase
```
rate()          - Taxa de crescimento por segundo [com range]
increase()      - Aumento total [com range]
irate()         - Instantaneous rate [com range]
resets()        - Quantas vezes counter resetou [com range]
```

### Funções de Histograma
```
histogram_quantile(φ, metric)  - Percentil do histograma
```

### Funções de Tempo
```
time()          - Timestamp atual em segundos
days_in_month() - Dias no mês
hour()          - Hora
day_of_month()  - Dia do mês
```

### Funções de Transformação
```
abs()           - Valor absoluto
ceil()          - Arredondar para cima
floor()         - Arredondar para baixo
round()         - Arredondar
sqrt()          - Raiz quadrada
log()           - Logaritmo natural
ln()            - Logaritmo natural
```

### Exemplos de Uso

```promql
# Taxa por segundo em 1 minuto
rate(http.server.requests_seconds_count[1m])

# Percentual de aumento em 5 minutos
increase(logback.events_total[5m]) / 300 * 100

# P99 latência
histogram_quantile(0.99, rate(http.server.requests_seconds_bucket[5m]))

# Top 5 endpoints mais lentos
topk(5, histogram_quantile(0.95, rate(http.server.requests_seconds_bucket[5m])))

# Soma por label
sum by (method) (rate(http.server.requests_seconds_count[1m]))

# Sem um label específico
sum without (instance) (rate(http.server.requests_seconds_count[1m]))

# Divisão com matching
(rate(http.server.requests_seconds_count{outcome="SERVER_ERROR"}[1m]) / 
 rate(http.server.requests_seconds_count[1m])) * 100
```

---

## 🛠️ Configuração Mínima (application.yaml)

```yaml
# Spring Boot 3.x
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      livenessState:
        enabled: true
      readinessState:
        enabled: true
  metrics:
    enable:
      all: true
    tags:
      application: ${spring.application.name}
      environment: ${app.environment:dev}
    export:
      prometheus:
        enabled: true
        step: 1m  # Intervalo de export

spring:
  application:
    name: seu-servico-aqui
```

---

## 🚀 Endpoints Disponíveis

### Com `management.endpoints.web.exposure.include=health,info,metrics,prometheus`

| Endpoint | Descrição |
|----------|-----------|
| `GET /actuator/health` | Health status básico |
| `GET /actuator/health/live` | Liveness probe (K8s) |
| `GET /actuator/health/ready` | Readiness probe (K8s) |
| `GET /actuator/info` | Info aplicação |
| `GET /actuator/metrics` | Lista de métricas |
| `GET /actuator/metrics/{metric}` | Detalhes de uma métrica |
| `GET /actuator/prometheus` | **← Scrape do Prometheus** |

### Exemplo de Resposta `/actuator/metrics`
```json
{
  "names": [
    "http.server.requests",
    "jvm.memory.used",
    "jvm.gc.pause",
    "process.cpu.usage",
    ...
  ]
}
```

### Exemplo de Resposta `/actuator/metrics/jvm.memory.used`
```json
{
  "name": "jvm.memory.used",
  "description": "The amount of used memory",
  "baseUnit": "bytes",
  "measurements": [
    {
      "statistic": "VALUE",
      "value": 156000000
    }
  ],
  "availableTags": [
    {
      "tag": "area",
      "values": ["heap", "nonheap"]
    },
    {
      "tag": "id",
      "values": ["G1 Eden Space", "G1 Old Generation"]
    }
  ]
}
```

---

## 📋 Prometheus Configuration para Spring Boot

### `prometheus.yml` - Job Configuration

```yaml
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    cluster: 'meu-cluster'
    environment: 'producao'

scrape_configs:
  - job_name: 'spring-boot'
    static_configs:
      - targets: ['localhost:8080']
        labels:
          service: 'api-gateway'
          version: '1.0.0'
    
    metrics_path: '/actuator/prometheus'
    scrape_interval: 15s
    scrape_timeout: 10s
    
    # Relabeling (opcional)
    relabel_configs:
      - source_labels: [__address__]
        target_label: instance
    
    # Service discovery (opcional)
    # consul_sd_configs:
    #   - server: 'localhost:8500'
```

### Com Kubernetes Service Discovery

```yaml
scrape_configs:
  - job_name: 'kubernetes-pods'
    kubernetes_sd_configs:
      - role: pod
    
    relabel_configs:
      # Apenas pods com annotation prometheus=true
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus]
        action: keep
        regex: "true"
      
      # Usar endpoint específico se definido
      - source_labels: [__meta_kubernetes_pod_annotation_prometheus_port]
        action: replace
        target_label: __address__
        regex: '([^:]+)(?::\d+)?;(\d+)'
        replacement: '$1:$2'
      
      # Adicionar namespace
      - source_labels: [__meta_kubernetes_namespace]
        action: replace
        target_label: kubernetes_namespace
      
      # Adicionar pod name
      - source_labels: [__meta_kubernetes_pod_name]
        action: replace
        target_label: kubernetes_pod_name
```

---

## 🎓 Conceitos Importantes

### Tipos de Métrica (Micrometer)

1. **Counter** - Valor que só aumenta (requisições totais)
   ```promql
   rate(métrica_total[1m])
   ```

2. **Gauge** - Valor que pode subir ou descer (memória usada)
   ```promql
   métrica  # Use valor direto
   ```

3. **Timer** - Medição de tempo com histograma
   ```promql
   metric_seconds_count    # Total de eventos
   metric_seconds_sum      # Tempo total gasto
   metric_seconds_bucket   # Histograma (para percentis)
   ```

4. **Distribution Summary** - Distribuição de valores
   - Similar ao timer mas sem unidade de tempo

### Range Vector vs Instant Vector

```promql
# Instant Vector - valor no momento agora
jvm.memory.used

# Range Vector - valores em um período
rate(http.server.requests_seconds_count[5m])
```

### Matching Labels

```promql
# Matching exato
métrica{label="valor"}

# Matching com regex
métrica{label=~"valor.*"}

# Negação
métrica{label!="valor"}

# Negação com regex
métrica{label!~"valor.*"}
```

### By vs Without

```promql
# BY - agrupa pelos labels especificados
sum by (method, status) (rate(...[1m]))

# WITHOUT - agrupa por todos EXCETO os especificados
sum without (instance) (rate(...[1m]))
```

---

## 📚 Recursos Educacionais

### Micrometer
- **10-min intro:** https://micrometer.io/docs/guide/application-metrics
- **Comparison with competitors:** https://micrometer.io/docs/why-micrometer
- **Custom metrics:** https://micrometer.io/docs/concepts#_custom_metrics

### Prometheus
- **PromQL Querying:** https://prometheus.io/docs/prometheus/latest/querying/
- **Recording Rules:** https://prometheus.io/docs/prometheus/latest/configuration/recording_rules/
- **Alerting Rules:** https://prometheus.io/docs/prometheus/latest/configuration/alerting_rules/
- **Best Practices:** https://prometheus.io/docs/practices/naming/
- **Metric Types:** https://prometheus.io/docs/concepts/metric_types/

### Visualização
- **Grafana Dashboards:** https://grafana.com/grafana/dashboards/
- **Spring Boot Dashboard ID:** 4701
- **JVM Dashboard ID:** 741

---

## ✅ Checklist de Implementação

- [ ] Spring Boot com `spring-boot-starter-actuator`
- [ ] Micrometer Prometheus (`micrometer-registry-prometheus`)
- [ ] `application.yaml` configurado com endpoints expostos
- [ ] `/actuator/prometheus` retornando métricas em formato text
- [ ] Prometheus configurado para scrape do endpoint
- [ ] Alertas definidos (erro, latência, memória)
- [ ] Dashboards no Grafana criados
- [ ] Tags customizadas adicionadas (application, environment, version)
- [ ] Métricas de negócio adicionadas (custom)
- [ ] Documentação do dashboard mantida

---

## 🐛 Troubleshooting

### "Metrics endpoint não está respondendo"
```
management.endpoints.web.exposure.include precisa incluir "metrics" e "prometheus"
```

### "Prometheus não consegue scrape"
```
Verifique:
1. URL está correta: http://host:port/actuator/prometheus
2. Porta está exposta
3. Spring Boot está rodando
4. Não há autenticação bloqueando
```

### "Métrica não aparece"
```
1. Verifique endpoint /actuator/metrics
2. A métrica precisa ser usada para aparecer
3. Algumas métricas aparecem apenas após primeiro uso
```

### "Memory leak de métricas"
```
Problema: Tags com valores muito variados (user IDs, etc)
Solução: Usar tag mapping ou regex pattern para reduzir cardinality
```

---

## 📞 Comunidades e Suporte

- **Spring Boot Slack:** https://spring.io/slack
- **Micrometer GitHub Issues:** https://github.com/micrometer-metrics/micrometer/issues
- **Prometheus Community:** https://prometheus.io/community/
- **Stack Overflow Tags:** `micrometer`, `prometheus`, `spring-boot`

---

## 🔄 Updates e Versões

### Versões Recomendadas (2024-2025)

| Componente | Versão Recomendada | Release |
|-----------|------------------|---------|
| Spring Boot | 3.4.x | Feb 2025 |
| Micrometer | 1.14.x | Jan 2024 |
| Prometheus | 2.54.x | Feb 2025 |
| Grafana | 11.x | Dec 2024 |

### Verificar versões instaladas

```bash
# Maven
mvn dependency:tree | grep -i micrometer

# Gradle
./gradlew dependencies | grep micrometer

# Na aplicação
curl http://localhost:8080/actuator/metrics | grep -i version
```

---

**Última atualização:** Abril 2025
**Mantido por:** Seu Time DevOps/SRE

