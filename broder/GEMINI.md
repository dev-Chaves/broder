# Project Overview: broder

`broder` is a lightweight monitoring and alerting tool for metrics exposed via Prometheus, built using the **Quarkus** framework. It provides a RESTful API for managing Alarms, their associated History, and connecting to Prometheus datasources. The project follows a domain-oriented layered architecture and uses a lightweight SQLite database for persistence.

## Objective

To provide a simple alternative for small teams and early-stage projects requiring basic monitoring and fast alerts with low operational cost.

## Goals

- **Prometheus integration**: Manage Prometheus datasources and list available metrics.
- **Visual Alerting**: Create and manage alert rules through a UI without writing PromQL manually.
- **Alert Engine**: Periodically evaluate rules and trigger alerts.
- **History Management**: Track alert states (firing/resolved).
- **Notifications**: Support basic notification channels (e.g., webhooks).

## Non-Goals

- **Metric Storage**: Broder does not replace Prometheus and does not store time-series data.
- **TSDB replacement**: It is not a database for metrics.
- **Feature Parity with Zabbix/Grafana**: It focuses on a subset of features for simplicity and speed.

## Main Technologies

- **Java 21**: Utilizes modern Java features like Records for DTOs.
- **Quarkus 3.34.5**: Core framework for the backend.
- **Hibernate ORM with Panache**: Simplified data persistence for SQLite.
- **SQLite**: Local, file-based database (located at `./data/data.db`).
- **Jackson**: JSON serialization/deserialization for API and Prometheus queries.
- **Hibernate Validator**: Declarative data validation.

## Architecture & Structure

Prometheus -> Broder (Quarkus) -> SQLite
                   |
                   +--> React UI
                   +--> Notification Engine

The project is organized under `src/main/java/org/acme/domain/`:

- **`alarm/`**: Contains core logic for Alarm management.
  - `Alarm`: JPA Entity.
  - `AlarmRepository`: Panache repository for database operations.
  - `AlarmService`: Business logic and transactional operations.
  - `AlarmResource`: JAX-RS REST endpoints.
  - `dto/`: Java Records used for data transfer (e.g., `AlarmRequestDTO`).
- **`history/`**: Manages the history of alarm usages/triggers.
- **`shared/api/`**: Utility classes and interfaces for the API layer.

## Building and Running

### Development Mode
Enables live coding and access to the Dev UI (http://localhost:8080/q/dev/):
```shell
./mvnw quarkus:dev
```

### Build & Package
To produce a standard runnable JAR:
```shell
./mvnw package
```

### Native Executable
To compile the application into a native binary:
```shell
./mvnw package -Dnative
```

### Testing
To run the test suite:
```shell
./mvnw test
```

## Development Conventions

1.  **Domain Isolation**: Keep business logic within the `domain` packages.
2.  **DTOs**: Use Java **Records** for all Request/Response DTOs.
3.  **Repositories**: Use `PanacheRepository<Entity>` for database access.
4.  **Service Layer**: Encapsulate complex logic and transactions in `@ApplicationScoped` service classes.
5.  **REST Resources**:
    - Implement the `BaseResource` interface.
    - Use `@Valid` for request validation.
6.  **Persistence**: Database file at `./data/data.db`. Schema management is set to `update`.
