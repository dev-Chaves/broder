# Broder — Lightweight JVM Alerting for Prometheus

> A visual, lightweight alternative to complex monitoring stacks like Grafana + Alertmanager or Zabbix. Built for JVM-based applications that need fast, simple alerting without the operational overhead.

[![Quarkus](https://img.shields.io/badge/Backend-Quarkus%203.34-blue?logo=quarkus)](https://quarkus.io)
[![React](https://img.shields.io/badge/Frontend-React%2019-blue?logo=react)](https://react.dev)
[![Prometheus](https://img.shields.io/badge/Metrics-Prometheus-orange?logo=prometheus)](https://prometheus.io)

---

## Screenshots

> *(Screenshots will be added before public launch)*

| Alarm Builder | Dashboard | History |
|---|---|---|
| *Builder placeholder* | *Dashboard placeholder* | *History placeholder* |

---

## Features

- **Visual Alarm Builder** — Drag-and-drop interface to create alerts from 12+ JVM/HTTP/DB templates without writing PromQL
- **Real-time Alarm Dashboard** — Zabbix-style cards with firing pulse animations, severity badges, and instant enable/disable
- **Alert History** — Paginated history with filtering, search, and "Top Triggered" statistics
- **Prometheus Proxy** — Built-in proxy for instant/range queries, labels, and label values
- **Scheduler-based Evaluation** — Automatic alarm evaluation every 20 seconds with error recovery
- **Onboarding Tour** — Guided first-time experience for new users
- **k6 Load Testing** — Comprehensive stress tests that validate alarm firing under real load

---

## Quick Start

The fastest way to try Broder is with Docker Compose. It spins up the entire stack:

```bash
# Clone the repository
git clone <repo-url>
cd broder

# Start the full stack (backend + frontend + Prometheus + demo app)
docker compose up

# Wait 30 seconds for services to initialize, then open:
# http://localhost:3000
```

The stack includes:
- **Broder Backend** at `http://localhost:8080`
- **Broder Frontend** at `http://localhost:3000`
- **Prometheus** at `http://localhost:9090`
- **Demo Spring Boot App** at `http://localhost:8181` (generates metrics for testing)

---

## Development

### Prerequisites

- Java 21
- Maven 3.9+
- Node.js 22+ and pnpm
- Docker & Docker Compose

### Backend

```bash
cd broder
./mvnw quarkus:dev
```

Backend runs on `http://localhost:8080` with live reload and Dev UI at `http://localhost:8080/q/dev`.

### Frontend

```bash
cd broder-front
pnpm install
pnpm dev
```

Frontend runs on `http://localhost:3000` with Vite proxy to the backend.

### Prometheus + Demo App

```bash
cd api-prometheus
docker compose up -d
```

---

## Production (PostgreSQL)

By default, Broder uses SQLite for development. For production, use the `prod` profile with PostgreSQL:

```bash
# Start PostgreSQL
docker run -d \
  --name broder-postgres \
  -e POSTGRES_USER=broder \
  -e POSTGRES_PASSWORD=broder \
  -e POSTGRES_DB=broder \
  -p 5432:5432 \
  postgres:16-alpine

# Run with prod profile
cd broder
./mvnw quarkus:dev -Dquarkus.profile=prod
```

Or via environment variables:
```bash
export QUARKUS_PROFILE=prod
export POSTGRES_HOST=localhost
export POSTGRES_PORT=5432
export POSTGRES_DB=broder
export POSTGRES_USER=broder
export POSTGRES_PASSWORD=broder
./mvnw quarkus:dev
```

---

## Architecture

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   React UI      │◄────►│  Broder Backend │◄────►│   Prometheus    │
│  (Port 3000)    │      │   (Port 8080)   │      │   (Port 9090)   │
└─────────────────┘      └─────────────────┘      └─────────────────┘
                                │                           │
                                ▼                           ▼
                         ┌─────────────────┐      ┌─────────────────┐
                         │  SQLite (file)  │      │ Webhook targets │
                         │  ./data/data.db │      │ (Slack, Pager,  │
                         └─────────────────┘      │ Discord, etc.)  │
                                                  └─────────────────┘
```

---

## Observability

Broder exposes production-ready health checks and Prometheus-compatible metrics out of the box.

### Health Checks

| Endpoint | Purpose |
|----------|---------|
| `GET /q/health` | Overall health status |
| `GET /q/health/live` | Liveness probe (Kubernetes) |
| `GET /q/health/ready` | Readiness probe (Kubernetes) |

The readiness check includes a database connectivity verification. All endpoints return `200 UP` when healthy.

### Metrics

Prometheus metrics are available at:

```
http://localhost:8080/q/metrics
```

Included metrics:
- **JVM metrics** — memory, GC, threads (Micrometer default)
- **HTTP metrics** — request duration and count per endpoint
- **`broder_alarms_evaluated_total`** — Counter of alarm evaluations
- **`broder_alarms_firing`** — Gauge of currently firing alarms
- **`broder_scheduler_duration_seconds`** — Histogram of scheduler cycle duration

---

## Webhook Notifications

Each alarm can optionally send a `POST` webhook when it transitions to `FIRING`, `RESOLVED`, or `ERROR`.

Set the `webhookUrl` field when creating or updating an alarm. If no URL is configured, the alarm logs only (no webhook sent).

### Payload Format

```json
{
  "alarmId": 1,
  "alarmName": "CPU Usage High",
  "status": "FIRING",
  "severity": "HIGH",
  "currentValue": 0.92,
  "message": "Alarm 'CPU Usage High' is firing. Current value: 0.92 > threshold: 0.8",
  "timestamp": "2026-04-28T16:45:00"
}
```

### Slack Integration

Create an [Incoming Webhook](https://api.slack.com/messaging/webhooks) in Slack and paste the URL into the alarm's `webhookUrl` field. The payload includes a `message` field compatible with Slack's `text` parameter.

Configuration:
```yaml
# application.yaml
broder:
  webhook:
    timeout-seconds: 5   # default
```

Webhook failures are logged but never block alarm evaluation or history recording.

---

## API Documentation

OpenAPI/Swagger UI is available at:

```
http://localhost:8080/q/swagger-ui
```

---

## Project Structure

```
broder/
├── broder/              # Quarkus backend (Java 21)
├── broder-front/        # React + TypeScript + Vite frontend
├── api-prometheus/      # Reference Spring Boot app + Prometheus (Docker)
├── docker-compose.yml   # Full stack orchestration
└── PLANO_BETA_v0.1.md   # Beta readiness plan (Portuguese)
```

---

## License

MIT

---

> **Status:** v0.1 Beta — actively developed. See [PLANO_BETA_v0.1.md](./PLANO_BETA_v0.1.md) for roadmap.
