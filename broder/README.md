# broder

Broder is a lightweight monitoring and alerting tool for metrics exposed via Prometheus. It simplifies the creation of alerts and basic visualization without the overhead of more complex ecosystems.

## Objective

To provide a simple alternative for small teams and early-stage projects requiring basic monitoring and fast alerts with low operational cost.

## Problem Solved

Traditional monitoring tools (Zabbix, Prometheus + Grafana + Alertmanager) often require extensive configuration, deep technical knowledge (PromQL, pipelines), and the maintenance of multiple components. Broder focuses on:

- Fast setup
- Visual alert creation via UI
- Lightweight local execution

## Goals

- Prometheus datasource management
- Metric listing from Prometheus
- UI-driven alert creation
- Periodic evaluation of alert rules
- Alert history management (firing/resolved)
- Basic notifications (webhooks)

## Non-Goals

- Replacing Prometheus (Broder does not store metrics)
- Being a TSDB
- Competing with all features of Zabbix or Grafana

## Stack

- **Backend**: Quarkus
- **Frontend**: React + TypeScript
- **Database**: SQLite
- **Metric Source**: Prometheus (via HTTP API)

## Architecture

Prometheus -> Broder (Quarkus) -> SQLite
                   |
                   +--> React UI
                   +--> Notification Engine

Prometheus remains the source of truth for metrics. Broder acts as the alert motor and user interface. SQLite stores configurations and state.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

NOTE: Quarkus ships with a Dev UI, available in dev mode only at http://localhost:8080/q/dev/.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

To build an uber-jar:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an uber-jar, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/broder-1.0.0-SNAPSHOT-runner`
