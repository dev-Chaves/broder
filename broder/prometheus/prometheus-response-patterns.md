# Padrões de Resposta Prometheus + Micrometer

## Formatos Esperados da API Prometheus

### 1. **Sucesso: `resultType: "vector"` (Instant Query)**
Quando você faz uma query instantânea (sem `[range]`):
```
GET /api/v1/query?query=http_server_requests_seconds_count
```

Resposta esperada:
```json
{
  "status": "success",
  "data": {
    "resultType": "vector",
    "result": [
      {
        "metric": {
          "__name__": "http_server_requests_seconds_count",
          "application": "api-prometheus",
          "job": "spring-boot-ref",
          "instance": "ref:3000",
          "method": "GET",
          "status": "200",
          "uri": "/metrics"
        },
        "value": [1776728920.725, "10"]
      }
    ]
  }
}
```

**Características:**
- `value`: array com `[timestamp_unix, valor_como_string]`
- `resultType`: sempre `"vector"` para instant queries
- Um ou mais elementos em `result`

---

### 2. **Range Query (Séries Temporais)**
Quando você faz uma query com range `[range]`:
```
GET /api/v1/query_range?query=http_server_requests_seconds_count[5m]&start=1609459200&end=1609462800&step=60
```

Resposta esperada:
```json
{
  "status": "success",
  "data": {
    "resultType": "matrix",
    "result": [
      {
        "metric": {
          "__name__": "http_server_requests_seconds_count",
          "job": "spring-boot-ref",
          "instance": "ref:3000"
        },
        "values": [
          [1776728920, "10"],
          [1776728980, "12"],
          [1776729040, "14"]
        ]
      }
    ]
  }
}
```

**Características:**
- `resultType`: `"matrix"`
- `values`: array de arrays (múltiplos pontos de dados)
- Retorna a evolução da métrica ao longo do tempo

---

### 3. **Erro: `status: "error"`**
```json
{
  "status": "error",
  "errorType": "bad_data",
  "error": "1:7: parse error: unexpected character: '('"
}
```

Outros tipos comuns:
- `bad_data` — query mal formada
- `internal` — erro interno do Prometheus
- `unavailable` — Prometheus indisponível
- `timeout` — query excedeu timeout

---

## Classes Java para Mapeamento

### Estrutura com Records (Recomendado para Quarkus)

```java
package com.benefix.prometheus.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

// Response raiz
public record PrometheusResponse(
    String status,
    PrometheusData data,
    String errorType,
    String error
) {}

// Contém result + resultType
public record PrometheusData(
    String resultType,
    List<PrometheusResult> result,
    List<PrometheusMatrixResult> matrix
) {}

// Para instant queries (vector)
public record PrometheusResult(
    Map<String, String> metric,
    PrometheusValue value
) {}

// value é [timestamp, valor]
public record PrometheusValue(
    @JsonProperty("0")
    long timestamp,
    @JsonProperty("1")
    String value
) {}

// Para range queries (matrix)
public record PrometheusMatrixResult(
    Map<String, String> metric,
    List<PrometheusValue> values
) {}

// DTO de resposta da sua API
public record MetricSnapshot(
    String metricName,
    Map<String, String> labels,
    String value,
    long timestampUnix
) {}
```

### Alternativa: Classes Mutáveis (Se preferir)

```java
public class PrometheusResponse {
    private String status;
    private PrometheusData data;
    private String errorType;
    private String error;
    
    // getters/setters...
}

public class PrometheusData {
    private String resultType;
    private List<PrometheusResult> result;
    private List<PrometheusMatrixResult> matrix;
    
    // getters/setters...
}

public class PrometheusResult {
    private Map<String, String> metric;
    private List<Object> value; // [timestamp, string_value]
    
    // getters/setters...
}
```

---

## Estratégia de Mapeamento para sua API

### Opção 1: Passthrough (Mínima Transformação)
```java
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    private final PrometheusClient prometheusClient;
    
    @GetMapping("/query")
    public PrometheusResponse query(@RequestParam String query) {
        return prometheusClient.instantQuery(query);
    }
}
```
**Quando usar:** Você quer passar os dados como-estão, sem processamento.

---

### Opção 2: Normalização (Recomendado)
```java
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    private final PrometheusClient prometheusClient;
    
    @GetMapping("/query")
    public List<MetricSnapshot> query(@RequestParam String query) {
        PrometheusResponse response = prometheusClient.instantQuery(query);
        
        if (!response.status().equals("success")) {
            throw new PrometheusException(response.error());
        }
        
        return response.data().result().stream()
            .map(r -> new MetricSnapshot(
                r.metric().get("__name__"),
                r.metric(),
                r.value().value(),
                r.value().timestamp()
            ))
            .toList();
    }
}
```

---

### Opção 3: Separar por Tipo (Melhor Prática)
```java
@RestController
@RequestMapping("/api/metrics")
public class MetricsController {
    
    private final PrometheusClient client;
    
    // Instant query
    @GetMapping("/instant")
    public ApiResponse<List<MetricSnapshot>> instantQuery(
        @RequestParam String query
    ) {
        var response = client.instantQuery(query);
        validateSuccess(response);
        return ApiResponse.success(normalize(response));
    }
    
    // Range query
    @GetMapping("/range")
    public ApiResponse<List<TimeSeriesSnapshot>> rangeQuery(
        @RequestParam String query,
        @RequestParam long start,
        @RequestParam long end,
        @RequestParam(defaultValue = "60") long step
    ) {
        var response = client.rangeQuery(query, start, end, step);
        validateSuccess(response);
        return ApiResponse.success(normalizeRange(response));
    }
    
    private void validateSuccess(PrometheusResponse response) {
        if (!"success".equals(response.status())) {
            throw new PrometheusException(
                response.errorType(), 
                response.error()
            );
        }
    }
}
```

---

## Cliente HTTP para Chamar Prometheus

### Usando `RestClient` (Spring 6.1+)
```java
@Configuration
public class PrometheusConfig {
    
    @Bean
    public RestClient prometheusClient(RestClientBuilder builder) {
        return builder
            .baseUrl("http://prometheus:9090")
            .defaultStatusHandler(HttpStatusCode::is4xxClientError, 
                (req, res) -> {
                    throw new PrometheusException("Client error: " + res.getStatusCode());
                })
            .defaultStatusHandler(HttpStatusCode::is5xxServerError,
                (req, res) -> {
                    throw new PrometheusException("Server error: " + res.getStatusCode());
                })
            .build();
    }
}

@Component
public class PrometheusClient {
    
    private final RestClient client;
    private final ObjectMapper mapper;
    
    public PrometheusClient(RestClient client, ObjectMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }
    
    public PrometheusResponse instantQuery(String query) {
        return client.get()
            .uri("/api/v1/query?query={query}", query)
            .retrieve()
            .body(PrometheusResponse.class);
    }
    
    public PrometheusResponse rangeQuery(
        String query, long start, long end, long step
    ) {
        return client.get()
            .uri("/api/v1/query_range?" +
                "query={query}&start={start}&end={end}&step={step}",
                query, start, end, step)
            .retrieve()
            .body(PrometheusResponse.class);
    }
}
```

### Alternativa: WebClient (Reactive)
```java
@Component
public class PrometheusClient {
    
    private final WebClient client;
    
    public PrometheusClient(WebClient.Builder builder) {
        this.client = builder.baseUrl("http://prometheus:9090").build();
    }
    
    public Mono<PrometheusResponse> instantQuery(String query) {
        return client.get()
            .uri("/api/v1/query?query={query}", query)
            .retrieve()
            .bodyToMono(PrometheusResponse.class)
            .onErrorMap(e -> new PrometheusException(e.getMessage(), e));
    }
}
```

---

## DTO de Resposta Padronizado da Sua API

```java
public record ApiResponse<T>(
    String status,           // "success" ou "error"
    T data,
    String errorCode,        // null se sucesso
    String errorMessage      // null se sucesso
) {
    
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null, null);
    }
    
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>("error", null, code, message);
    }
}

// Uso:
@GetMapping("/metrics")
public ApiResponse<List<MetricSnapshot>> getMetrics(
    @RequestParam String query
) {
    try {
        var prometheusResponse = client.instantQuery(query);
        var normalized = normalize(prometheusResponse);
        return ApiResponse.success(normalized);
    } catch (PrometheusException e) {
        return ApiResponse.error(e.getErrorType(), e.getMessage());
    }
}
```

---

## Checklist de Implementação

- [ ] Criar DTOs para `PrometheusResponse`, `PrometheusData`, `PrometheusResult`
- [ ] Escolher formato (passthrough vs normalizado)
- [ ] Implementar `PrometheusClient` com tratamento de erros
- [ ] Criar endpoints `/api/metrics/instant` e `/api/metrics/range`
- [ ] Adicionar `@ExceptionHandler` para `PrometheusException`
- [ ] Testar com queries reais do seu Prometheus
- [ ] Documentar queries esperadas (ex: `http_server_requests_seconds_count`)
- [ ] Adicionar validação de `resultType` antes de processar

