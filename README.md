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

## Architecture

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│   React UI      │◄────►│  Broder Backend │◄────►│   Prometheus    │
│  (Port 3000)    │      │   (Port 8080)   │      │   (Port 9090)   │
└─────────────────┘      └─────────────────┘      └─────────────────┘
                                │
                                ▼
                         ┌─────────────────┐
                         │  SQLite (file)  │
                         │  ./data/data.db │
                         └─────────────────┘
```

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
