# Home Energy Tracker

A microservices-based Spring Boot application that monitors household energy consumption across smart home devices, triggers threshold-based email alerts, and delivers AI-powered energy-saving insights via a locally-running LLM.

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Services](#services)
- [Technology Stack](#technology-stack)
- [Data Flow](#data-flow)
- [Database Schema](#database-schema)
- [API Reference](#api-reference)
- [Configuration](#configuration)
- [Getting Started](#getting-started)
- [Running the Services](#running-the-services)
- [Built-in Data Simulation](#built-in-data-simulation)
- [Project Structure](#project-structure)

---

## Overview

Home Energy Tracker solves the problem of unmonitored household energy usage. It ingests real-time energy readings from smart home devices, stores time-series data in InfluxDB, aggregates consumption per user, fires email alerts when a user-defined threshold is exceeded, and uses an Ollama-hosted LLM to generate personalised energy-saving tips and usage overviews.

---

## Architecture

The system is composed of seven independent Spring Boot 4 services communicating over HTTP (synchronous) and Apache Kafka (asynchronous).

```
Client / Browser
       │
       ▼
  ┌──────────┐
  │ API      │  :9000  Spring Cloud Gateway (MVC) + Resilience4j circuit breakers
  │ Gateway  │
  └────┬─────┘
       │ routes to downstream services
  ┌────┴──────────────────────────────────────────────────┐
  │                                                       │
  ▼                   ▼                   ▼               ▼
User Service     Device Service    Ingestion Service   Insight Service
  :8080             :8081               :8082             :8085
  MySQL             MySQL               │                 │
  Flyway                                │ Kafka           │ HTTP
                                        ▼                 ▼
                                   Usage Service      Ollama (LLM)
                                      :8083
                                      InfluxDB
                                      │ Kafka
                                      ▼
                                  Alert Service
                                     :8084
                                     MySQL
                                     Mailpit (SMTP)
```

---

## Services

| Service | Port | Responsibility |
|---|---|---|
| `api-gateway` | 9000 | Single entry point; routes all `/api/v1/**` traffic with per-service circuit breakers |
| `user-service` | 8080 | CRUD for users; stores alerting preferences and energy thresholds |
| `device-service` | 8081 | CRUD for smart home devices; associates devices to users |
| `ingestion-service` | 8082 | Accepts energy readings via REST and publishes them to Kafka topic `energy-usage` |
| `usage-service` | 8083 | Consumes `energy-usage` events, writes to InfluxDB, aggregates hourly per user, publishes `energy-usage-alerting` events when thresholds are exceeded |
| `alert-service` | 8084 | Consumes `energy-usage-alerting` events and sends threshold-breach emails via SMTP |
| `insight-service` | 8085 | Fetches usage data from `usage-service` and calls Ollama (deepseek-coder model) to generate energy-saving tips and usage overviews |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.1 |
| API Gateway | Spring Cloud Gateway (MVC) 2025.1.3 |
| Resilience | Resilience4j circuit breakers |
| Persistence (relational) | Spring Data JPA + Hibernate + MySQL 8.3 |
| Schema migrations | Flyway (user-service manages shared schema) |
| Time-series storage | InfluxDB 2.7 (influxdb-client-java 6.12.0) |
| Messaging | Apache Kafka (KRaft mode, no ZooKeeper) |
| AI / LLM | Spring AI 2.0.1 + Ollama (`deepseek-coder` model) |
| Email | Spring Mail + Mailpit (local SMTP dev server) |
| Security | Spring Security + OAuth2 Resource Server (JWT via Keycloak) on the gateway; downstream services are unauthenticated internally |
| API Documentation | SpringDoc OpenAPI 3.0.2 (Swagger UI aggregated at the gateway) |
| Metrics | Micrometer + Prometheus (user-service, device-service, api-gateway) |
| Object mapping | ModelMapper 3.2.4 |
| AOP | Spring AOP / AspectJ (logging + execution timing) |
| Observability | Spring Actuator, Logback (rolling file + error file appenders) |
| Containerisation | Docker Compose |
| Build | Maven (per-service `mvnw` wrappers) |
| Lombok | Yes (all services) |

---

## Data Flow

### Ingestion → Storage

1. A client (or the built-in simulator) `POST /api/v1/ingestion` with `{ deviceId, energyConsumed, timestamp }`.
2. `ingestion-service` publishes an `EnergyUsageEvent` to Kafka topic **`energy-usage`**.
3. `usage-service` consumes the event and writes a time-series point to InfluxDB (`energy-usage` measurement, tagged by `deviceId`).

### Threshold Alerting

4. Every 10 seconds `usage-service` runs a scheduled job that:
   - Queries InfluxDB for the last hour of consumption, grouped and summed by `deviceId`.
   - Calls `device-service` to resolve each `deviceId` → `userId`.
   - Calls `user-service` to fetch each user's alerting flag and threshold.
   - For users whose total consumption exceeds their threshold, publishes an `AlertingEvent` to Kafka topic **`energy-usage-alerting`**.
5. `alert-service` consumes the event and sends an email via SMTP (Mailpit in development).

### AI Insights

6. A client calls `GET /api/v1/insight/saving-tips/{userId}` or `GET /api/v1/insight/overview/{userId}`.
7. `insight-service` fetches the last 3 days of usage from `usage-service`, builds a prompt, and calls the locally-running Ollama `deepseek-coder` model via Spring AI.
8. The LLM response is returned to the caller as an `InsightDto`.

---

## Database Schema

Managed by Flyway migrations in `user-service/src/main/resources/db/migration/`.

**`users`**
| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `firstname` | VARCHAR(100) | Required |
| `lastname` | VARCHAR(100) | |
| `email` | VARCHAR(255) | Unique |
| `address` | TEXT | |
| `alerting` | TINYINT(1) | 0 = disabled |
| `energy_alerting_threshold` | DOUBLE | kWh threshold for alerts |

**`devices`**
| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `name` | VARCHAR(255) | |
| `type` | VARCHAR(50) | Enum: `SPEAKER`, `CAMERA`, `THERMOSTAT`, `LIGHT`, `LOCK`, `DOORBELL` |
| `location` | VARCHAR(255) | |
| `user_id` | BIGINT FK | → `users.id` ON DELETE CASCADE |

**`alerts`**
| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT PK | Auto-increment |
| `user_id` | BIGINT | |
| `sent` | TINYINT(1) | |
| `created_at` | TIMESTAMP | Default: current timestamp |

InfluxDB stores energy readings in the **`usage-bucket`** bucket under the `energy-usage` measurement with a `deviceId` tag and `energyConsumed` field.

---

## API Reference

All services are accessed through the API Gateway at `http://localhost:9000`. Each service also exposes its own port directly.

### User Service — `/api/v1/users`

| Method | Path | Description |
|---|---|---|
| `POST` | `/users` | Create a user |
| `GET` | `/users/{id}` | Get user by ID |
| `PUT` | `/users/{id}` | Update user |
| `DELETE` | `/users/{id}` | Delete user |

### Device Service — `/api/v1/devices`

| Method | Path | Description |
|---|---|---|
| `POST` | `/devices` | Register a device |
| `GET` | `/devices/{id}` | Get device by ID |
| `PUT` | `/devices/{id}` | Update device |
| `DELETE` | `/devices/{id}` | Delete device |
| `GET` | `/devices/user/{userId}` | List all devices for a user |

### Ingestion Service — `/api/v1/ingestion`

| Method | Path | Description |
|---|---|---|
| `POST` | `/ingestion` | Submit an energy reading |

Request body:
```json
{
  "deviceId": 1,
  "energyConsumed": 2.45,
  "timestamp": "2025-01-01T12:00:00Z"
}
```

### Usage Service — `/api/v1/usage`

| Method | Path | Description |
|---|---|---|
| `GET` | `/usage/{userId}?days=3` | Get aggregated device energy usage for a user over N days (default: 3) |

### Insight Service — `/api/v1/insight`

| Method | Path | Description |
|---|---|---|
| `GET` | `/insight/saving-tips/{userId}` | AI-generated energy-saving tips based on last 3 days of usage |
| `GET` | `/insight/overview/{userId}` | AI-generated usage overview and comparison to average households |

---

## Configuration

Authentication is handled at the **API Gateway** using OAuth2/JWT. The gateway validates Bearer tokens against a Keycloak realm. Downstream services do not perform their own authentication.

Key configuration properties per service:

**`api-gateway`**
```yaml
spring.security.oauth2.resourceserver.jwt.issuer-uri: http://localhost:8091/realms/het-security-realm
keycloak.auth.jwk-set-uri: http://localhost:8091/realms/het-security-realm/protocol/openid-connect/certs
excluded.urls:   # paths that bypass JWT auth (actuator, swagger)
  - /actuator/**
  - /swagger-ui/**
  - /v3/api-docs/**
```

**`usage-service`**
```yaml
influx:
  url: http://localhost:8072
  token: <influxdb-token>
  org: teamengineoil
  bucket: usage-bucket

device.service.url: http://localhost:8081/api/v1/devices
user.service.url:   http://localhost:8080/api/v1/users
```

**`ingestion-service`**
```yaml
simulation:
  requests-per-interval: 100
  interval-ms: 10000
  parallel-threads: 10
```

**`insight-service`**
```yaml
spring.ai.ollama.chat.model: deepseek-coder
usage.service.url: http://localhost:8083/api/v1/usage
```

**`alert-service`**
```yaml
spring.mail.host: localhost
spring.mail.port: 1025   # Mailpit
```

**`api-gateway` — Resilience4j circuit breaker defaults**
```yaml
slidingWindowSize: 8
failureRateThreshold: 20        # %
waitDurationInOpenState: 5s
permittedNumberOfCallsInHalfOpenState: 2
```

**Swagger UI (aggregated at gateway)**

The gateway aggregates OpenAPI docs from `user-service` and `device-service`:

```
http://localhost:9000/swagger-ui.html
```

Individual service docs are also available directly:
- `http://localhost:8080/api/v1/swagger-ui/index.html` (user-service)
- `http://localhost:8081/api/v1/swagger-ui/index.html` (device-service)

---

## Getting Started

### Prerequisites

| Tool | Version |
|---|---|
| Java | 21+ |
| Maven | 3.9+ (or use included `mvnw`) |
| Docker & Docker Compose | Latest |
| Ollama | Latest — with `deepseek-coder` model pulled |
| Keycloak | Running on port `8091` with realm `het-security-realm` configured |

### 1. Start infrastructure

```bash
docker compose up -d
```

This starts:
- **MySQL** on port `3308` (mapped from 3306 inside container)
- **Kafka** (KRaft) on ports `9092` (internal) / `9094` (host)
- **Kafka UI** on port `8070` → http://localhost:8070
- **InfluxDB** on port `8072` → http://localhost:8072
- **Mailpit** on port `8025` (UI) / `1025` (SMTP) → http://localhost:8025

> **Note:** Keycloak is not included in `docker-compose.yml`. It must be started separately and configured with the `het-security-realm` realm before the gateway will accept requests.

### 2. Pull the Ollama model

```bash
ollama pull deepseek-coder
```

### 3. Build and run each service

From each service directory (or use your IDE):

```bash
cd user-service && ./mvnw spring-boot:run
cd device-service && ./mvnw spring-boot:run
cd ingestion-service && ./mvnw spring-boot:run
cd usage-service && ./mvnw spring-boot:run
cd alert-service && ./mvnw spring-boot:run
cd insight-service && ./mvnw spring-boot:run
cd api-gateway && ./mvnw spring-boot:run
```

> **Note:** `user-service` must start before `device-service` and `alert-service` because Flyway runs the shared schema migrations (`users`, `devices`, `alerts` tables) on startup.

---

## Running the Services

### Service startup order

1. `user-service` (runs Flyway migrations)
2. `device-service`, `alert-service` (depend on schema)
3. `ingestion-service`, `usage-service`
4. `insight-service`
5. `api-gateway`

### Verify health

```bash
curl http://localhost:9000/actuator/health
```

---

## Built-in Data Simulation

`ingestion-service` ships with two simulators for development and load testing. They are disabled by default (the `@Scheduled` annotation on `ContinuousDataSimulator` is commented out; `ParallelDataSimulator` runs on a fixed schedule).

**`ParallelDataSimulator`** — active by default:
- Fires every `simulation.interval-ms` (default: 10 000 ms).
- Sends `simulation.requests-per-interval` (default: 100) requests split across `simulation.parallel-threads` (default: 10) threads.
- Generates random `deviceId` (1–199) and `energyConsumed` (0.00–10.00 kWh) values.

To disable simulation, remove or comment out the `@Scheduled` annotation on `ParallelDataSimulator.sendMockData()`.

---

## Project Structure

```
home-energy-tracker/
├── api-gateway/            # Spring Cloud Gateway — routing + circuit breakers
├── user-service/           # User management + Flyway migrations
├── device-service/         # Device registry
├── ingestion-service/      # Energy reading ingestion + Kafka producer + simulators
├── usage-service/          # Kafka consumer + InfluxDB writer + alerting scheduler
├── alert-service/          # Kafka consumer + email notifications
├── insight-service/        # AI insights via Spring AI + Ollama
├── docker/
│   ├── mysql/init.sql      # Database initialisation
│   └── keycloak/           # Keycloak realm config (infrastructure placeholder)
├── docker-compose.yml      # Full infrastructure stack
└── Readme.md
```

Each service follows the same internal package layout:

```
com.teamengineoil.<service>/
├── config/         # Spring Security, beans
├── controller/     # REST controllers
├── service/        # Business logic
├── repository/     # Spring Data JPA repositories (where applicable)
├── entity/         # JPA entities (where applicable)
├── dto/            # Request/response DTOs
├── mapper/         # ModelMapper wrappers (where applicable)
├── exception/      # Custom exceptions + global handler (where applicable)
└── aspect/         # AOP logging + execution timing (user-service)
```
