# broder — Agent Notes

> Documento de referência rápida para agentes de código trabalhando no backend do `broder`. Atualizado após a refatoração de domínio rico (Rich Domain Model) e paginação.

---

## Objetivo

`broder` é uma ferramenta leve de monitoramento e alerting para métricas expostas via **Prometheus**. O backend (Quarkus + SQLite) fornece uma API REST completa para:

- Criar e gerenciar regras de alarme (com ou sem PromQL manual)
- Avaliar periodicamente essas regras consultando o Prometheus
- Registrar histórico de estados (firing, resolved, error)
- Notificar (por enquanto via logs estruturados)

O frontend React (não presente neste repo) consumirá essa API.

---

## Arquitetura do Backend

### Stack
- **Java 21** — Records para DTOs, switch expressions, pattern matching onde aplicável.
- **Quarkus 3.34.5** — Framework core.
- **Hibernate ORM + Panache** — Persistência simplificada.
- **SQLite** — Banco file-based (`./data/data.db`). Schema auto-gerenciado (`update`).
- **MicroProfile REST Client** — Comunicação com Prometheus.
- **Jackson** — JSON (de)serialização.
- **Hibernate Validator** — Validação declarativa nos DTOs.
- **SmallRye OpenAPI** — Documentação automática da API (`/q/swagger-ui`).

### Organização de Pacotes
```
org.acme.domain.
├── alarm/              ← Entidades, regras, scheduler, builder
│   ├── dto/            ← Records de request/response
│   ├── Alarm.java      ← Rich domain entity
│   ├── AlarmCondition.java
│   ├── ComparisonOperator.java
│   ├── AlarmService.java
│   ├── AlarmResource.java
│   ├── AlarmScheduler.java
│   ├── AlarmEvaluator.java
│   ├── AlarmEvaluationOrchestrator.java
│   ├── AlarmBuilderService.java
│   ├── PromQLBuilder.java
│   ├── MetricCatalog.java
│   └── ...
├── history/            ← Auditoria de avaliações
│   ├── History.java
│   ├── HistoryService.java
│   └── HistoryResource.java
├── prometheus/         ← Cliente e proxy para Prometheus
│   ├── client/
│   ├── PrometheusService.java
│   └── PrometheusResource.java
└── shared/api/         ← BaseResource, PageResponse, GlobalExceptionMapper
```

---

## Funcionamento do Domínio

### Alarm (Rich Entity)
`Alarm` não é mais um bean anêmico. Ele encapsula:
- **`AlarmCondition`** (`@Embedded`): composto por `query`, `ComparisonOperator`, `threshold`.
- **Comportamento de domínio**:
  - `evaluate(double currentValue)` → retorna `AlarmStatus` (FIRING, RESOLVED, ACTIVE, ERROR)
  - `recordEvaluation(AlarmStatus, LocalDateTime)` → atualiza timestamps, contadores
  - `wasFiring()`, `isTransitioningToFiring()`, `isTransitioningFromFiring()`

Nunca mais acesse `alarm.getQuery()` / `alarm.getComparison()` / `alarm.getThreshold()` diretamente — use `alarm.getCondition()`.

### AlarmCondition (Value Object)
- Validado no construtor: `query` não-blank, `operator` não-nulo, `threshold` numérico.
- `matches(double value)` → delega para `ComparisonOperator.apply()`.
- Mapeado via `@Embedded` + `@AttributeOverrides` nas colunas `query`, `comparison`, `threshold`.

### ComparisonOperator (Enum)
```java
GT(">"), LT("<"), GTE(">="), LTE("<="), EQ("==")
```
- `fromSymbol(String)` — parse seguro com mensagem de erro clara.
- `apply(double value, double threshold)` — EQ usa tolerância (`1e-9`).

### Fluxo de Avaliação (Scheduler)
1. `AlarmScheduler` (a cada 20s, `@Blocking`) busca alarmes habilitados.
2. Para cada alarme:
   - `AlarmEvaluator.evaluate(alarm)` → retorna `Uni<Double>` (valor bruto do Prometheus).
   - `alarm.evaluate(currentValue)` → entidade decide o novo status.
   - `AlarmEvaluationOrchestrator.process(...)` → persiste status, grava histórico, notifica.
3. O scheduler NÃO contém lógica de negócio — apenas orquestração e contagem de estatísticas.

### Histórico
- `History` usa `AlarmStatus` enum (não mais String).
- Factory method: `History.recordFor(alarm, status, value)`.
- Endpoints paginados: `GET /history`, `GET /history/alarm/{alarmId}`, `GET /history/alarm/{alarmId}/latest`.

---

## API REST

### OpenAPI / Swagger
- **Swagger UI:** `http://localhost:8080/q/swagger-ui`
- **OpenAPI JSON:** `http://localhost:8080/q/openapi`

### Paginação
Listagens retornam `PageResponse<T>`:
```json
{
  "content": [...],
  "totalElements": 100,
  "totalPages": 5,
  "page": 0,
  "size": 20
}
```
Parâmetros: `?page=0&size=20` (defaults).

### Endpoints Principais

#### Alarmes
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/alarms` | Listar alarmes paginados |
| GET | `/alarms/{id}` | Detalhes do alarme |
| POST | `/alarms` | Criar alarme |
| PUT | `/alarms/{id}` | Atualizar alarme |
| DELETE | `/alarms/{id}` | Remover alarme |

#### Alarm Builder
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/alarms/builder/templates` | Templates disponíveis |
| GET | `/alarms/builder/categories` | Categorias |
| GET | `/alarms/builder/metrics` | Métricas conhecidas |
| POST | `/alarms/builder/preview` | Preview com valor atual |
| POST | `/alarms/builder/validate` | Validar configuração |
| POST | `/alarms/builder` | Criar alarme via builder |

#### Histórico
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| GET | `/history` | Histórico geral paginado |
| GET | `/history/alarm/{alarmId}` | Histórico de um alarme |
| GET | `/history/alarm/{alarmId}/latest` | Último registro do alarme |
| POST | `/history` | Criação manual (admin/test) |

#### Prometheus Proxy
| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/prometheus/query` | Instant query |
| POST | `/prometheus/query/range` | Range query |
| GET | `/prometheus/labels` | Listar labels |
| GET | `/prometheus/labels/{name}/values` | Valores de uma label |

---

## Convenções de Código

1. **DTOs**: Java Records para todos os request/response. Nunca retorne entidades JPA diretamente.
2. **DTOs de Paginação**: Use `PageResponse<T>` (genérico) para listagens.
3. **Persistência**: `PanacheRepository<Entity>` em repositórios; injete em services `@ApplicationScoped`.
4. **Transações**: `@Transactional` em métodos de escrita nos services.
5. **REST Resources**: Implementam `BaseResource` (helpers `toCreated`, `toOk`, `toNoContent`). NÃO use `@ApplicationScoped` em resources JAX-RS.
6. **Validação**: `@Valid` nos parâmetros de request nos resources.
7. **Exception Handling**: `GlobalExceptionMapper` (`@Provider`) já mapeia 400, 404, 409, 500. Adicione novos mapeamentos lá, nunca `try/catch` por resource.
8. **Logging**: Use `org.jboss.logging.Logger`. Prefixe com tags: `[SERVICE]`, `[API]`, `[SCHEDULER]`, `[EVALUATOR]`, `[BUILDER]`, `[HISTORY]`, `[ORCHESTRATOR]`.

---

## Observações Críticas para Agentes

### 1. Nunca quebre a validação de `AlarmCondition`
O construtor de `AlarmCondition` valida que `threshold` é parseável como `double`. Se você precisar adicionar um novo tipo de threshold (ex: percentual com `%`), primeiro estenda o VO, não remova a validação.

### 2. `AlarmEvaluationResultDTO` foi removido
Não existe mais. O `AlarmEvaluator` retorna `Uni<Double>`. A lógica de transição de status vive em `Alarm.evaluate(double)`.

### 3. `@ApplicationScoped` em Resources é proibido
JAX-RS resources no Quarkus usam `@RequestScoped` por padrão. `@ApplicationScoped` foi removido de todos os resources. Não reintroduza.

### 4. SQLite e Schema
- Banco: `./data/data.db`
- Seed: `src/main/resources/database/data.db`
- Schema: `hibernate-orm.schema-management.strategy=update`
- **Se alterar entidades (novos campos, tipos, embeddables)**, apague `./data/data.db` para recriar o schema na próxima inicialização.

### 5. Prometheus Client é reativo
`PrometheusService` retorna `Uni<...>`. No scheduler (contexto `@Blocking`), use `.await().atMost(timeout)`. No resource `PrometheusResource`, retorne `Uni<Response>` diretamente.

### 6. Builder de Queries PromQL
`PromQLBuilder` centraliza a construção de queries. Se adicionar um novo template que use sintaxe PromQL incomum (ex: `sum()`, `avg()`), verifique se `extractMetricName()` precisa de ajuste.

### 7. Seed de Alarmes Padrão
`AlarmStartup` executa `seedDefaultAlarms()` no `@Observes StartupEvent`. Se o banco já tiver registros, ele pula. Para forçar re-seed: pare o app, delete `./data/data.db`, reinicie.

### 8. CORS já habilitado
Configurado para `http://localhost:3000` (React dev). Se o frontend usar outra porta/origin, atualize `application.yaml`:
```yaml
quarkus:
  http:
    cors:
      origins: http://localhost:SEU_PORTA
```

### 9. Não há testes ainda
`src/test/java/` está vazio. Se precisar adicionar testes:
- `@QuarkusTest` para testes de integração
- Testes unitários simples para `ComparisonOperator`, `AlarmCondition`, `Alarm.evaluate()`
- O LogManager do JBoss já está configurado no Surefire.

### 10. Resources não devem orquestrar
O resource deve chamar **um único método** do service. Orquestração entre múltiplos services fica no service layer (ex: `AlarmBuilderService.buildAndSave()`).

---

## Quick Commands

```bash
# Dev mode (live coding + Dev UI + Swagger UI)
./mvnw quarkus:dev

# Run tests
./mvnw test

# Build
./mvnw package

# Native build (também roda ITs)
./mvnw package -Dnative
```

---

## Relacionados

- `GEMINI.md` — visão geral do produto, goals e non-goals.
- `README.md` — instruções de build para humanos.
- `LOGGING_GUIDE.md` — guia de filtragem de logs por tag.
- `IMPLEMENTATION_REPORT_*.md` — relatórios de implementação (pode estar desatualizado pós-refatoração).
