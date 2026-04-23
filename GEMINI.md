# Project Overview: broder Workspace

This workspace contains the `broder` project, a lightweight monitoring and alerting tool designed to work with Prometheus metrics. The project aims to provide a simple, visual alternative to complex monitoring setups like Zabbix or Grafana + Alertmanager, focusing on fast setup and low operational cost.

## Workspace Structure

The workspace is organized into several directories:

-   **`broder 2/`**: The main backend application built with **Quarkus**. This is where the core logic, API, and persistence reside.
-   **`front-broder/`**: (Placeholder) Intended for the React + TypeScript frontend UI. Currently empty.
-   **`api-prometheus/`**: (Placeholder) Intended for Prometheus-related integration logic. Currently empty.
-   **`broder/`**: (Legacy/Empty) Contains only an `.idea` folder; all active code is in `broder 2/`.

## Core Application: Broder Backend (Quarkus)

The backend manages alarm rules, evaluates them against Prometheus metrics, and stores alert history.

### Main Technologies
-   **Java 21**: Leveraging modern features like Records for DTOs.
-   **Quarkus 3.34.5**: Core framework.
-   **Hibernate ORM with Panache**: Simplified data persistence.
-   **SQLite**: File-based database located at `broder 2/data/data.db`.
-   **JAX-RS (REST)**: API implementation using Quarkus REST.

### Architecture
Prometheus (external) -> Broder (Quarkus) -> SQLite
                               |
                               +--> React UI (front-broder)
                               +--> Notification Engine (webhooks)

## Building and Running

Commands should be executed within the `broder 2/` directory:

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
To compile into a native binary (requires GraalVM or container build):
```shell
./mvnw package -Dnative
```

### Testing
To run the test suite:
```shell
./mvnw test
```

## Development Conventions

1.  **Domain Isolation**: Keep business logic within `org.acme.domain` packages (e.g., `alarm`, `history`).
2.  **DTOs**: Use Java **Records** for all Request/Response data transfer objects.
3.  **Repositories**: Use `PanacheRepository<Entity>` for database access.
4.  **Service Layer**: Encapsulate complex logic and transactional operations in `@ApplicationScoped` service classes.
5.  **REST Resources**: Implement the `BaseResource` interface for consistent API responses.
6.  **Persistence**: Database schema is managed via `quarkus.hibernate-orm.schema-management.strategy=update`.

## Key Files
-   **`broder 2/pom.xml`**: Maven configuration and dependencies.
-   **`broder 2/src/main/resources/application.yaml`**: Quarkus and datasource configuration.
-   **`broder 2/src/main/java/org/acme/domain/`**: Core domain logic.
-   **`broder 2/data/data.db`**: SQLite database file.
