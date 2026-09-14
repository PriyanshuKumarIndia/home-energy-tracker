# AGENTS.md

## Project Overview

Home Energy Tracker is a **Spring Boot 4.1.1 microservices application** (Java 21) composed of seven independently deployable services. It ingests smart home device energy readings, stores time-series data in InfluxDB, fires threshold-based email alerts via Kafka, and generates AI-powered energy-saving insights using Spring AI + Ollama (`deepseek-coder`). There is no frontend — all interaction is via REST APIs.

---

## Architecture

Seven services, each its own Maven module with its own `pom.xml` and `mvnw`:

| Service | Port | Database | Role |
|---|---|---|---|
| `api-gateway` | 9000 | — | Spring Cloud Gateway MVC; routes + Resilience4j circuit breakers + OAuth2/JWT auth (Keycloak) |
| `user-service` | 8080 | MySQL | User CRUD; owns all Flyway migrations |
| `device-service` | 8081 | MySQL | Device CRUD |
| `ingestion-service` | 8082 | — | REST → Kafka producer (`energy-usage` topic) |
| `usage-service` | 8083 | InfluxDB | Kafka consumer; writes to InfluxDB; scheduled alerting publisher |
| `alert-service` | 8084 | MySQL | Kafka consumer; sends emails via SMTP |
| `insight-service` | 8085 | — | Calls `usage-service` via HTTP; calls Ollama LLM |

**Synchronous flow (HTTP):**
```
Client → api-gateway (:9000) → downstream service → [RestTemplate client → another service]
```

**Asynchronous flow (Kafka):**
```
ingestion-service → topic: energy-usage → usage-service → InfluxDB
usage-service (scheduled, every 10s) → topic: energy-usage-alerting → alert-service → SMTP
```

**AI flow:**
```
Client → insight-service → UsageClient (RestTemplate) → usage-service
                         → OllamaChatModel → Ollama (deepseek-coder)
```

Gateway routes are defined as `@Bean RouterFunction<ServerResponse>` in `api-gateway/src/main/java/.../route/`. Each route has a dedicated fallback returning `503 SERVICE_UNAVAILABLE` with a plain-text body.

The gateway also aggregates Swagger UI for `user-service` and `device-service` at `http://localhost:9000/swagger-ui.html`.

---

## Project Structure

Each service follows this internal layout under `com.teamengineoil.<service_name>/`:

```
config/       — ModelMapper bean, InfluxDB client, OllamaConfig, OpenApiConfig, SecurityConfig (gateway only)
controller/   — REST controllers (@RestController)
service/      — Business logic (@Service); also Kafka @KafkaListener methods live here
repository/   — Spring Data JPA (@Repository)
entity/       — JPA entities (@Entity)
dto/          — Request/response objects (mix of Lombok @Data classes and Java records)
mapper/       — ModelMapper wrappers (@Component)
exception/    — Custom exceptions + @RestControllerAdvice handler
aspect/       — AOP (user-service only: LoggingAspect, ExecutionTimeAspect)
```

Kafka event classes live in a **separate top-level package** `com.teamengineoil.kafka.event`, not inside the service package. Both producer and consumer services duplicate these classes locally — there is no shared library module.

> Note: `AlertingEvent` is a Java record in `usage-service` but a Lombok `@Data` class in `alert-service`. Keep this asymmetry when modifying event shapes.

---

## Development Workflow

**Start infrastructure first (required before any service):**
```bash
docker compose up -d
```
Starts: MySQL (:3308→3306), Kafka KRaft (:9094 host), Kafka UI (:8070), InfluxDB (:8072), Mailpit (:8025 UI / :1025 SMTP).

**Start Ollama (required for insight-service):**
```bash
ollama pull deepseek-coder
ollama serve
```

**Run a service:**
```bash
cd <service-dir> && ./mvnw spring-boot:run
# Windows:
cd <service-dir> && mvnw.cmd spring-boot:run
```

**Required startup order:**
1. `user-service` — runs Flyway, creates `users`, `devices`, `alerts` tables
2. `device-service`, `alert-service` — depend on schema existing
3. `ingestion-service`, `usage-service`
4. `insight-service`
5. `api-gateway`

**Build/package:**
```bash
cd <service-dir> && ./mvnw clean package -DskipTests
```

**Run tests:**
```bash
cd <service-dir> && ./mvnw test
```

**Health check (gateway):**
```bash
curl http://localhost:9000/actuator/health
```

---

## Code Conventions

- **Injection:** Constructor injection via `@RequiredArgsConstructor` (Lombok) throughout. No `@Autowired` field injection.
- **DTOs:** Two styles coexist — Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor` classes (user-service, device-service) and Java `record` with `@Builder` (usage-service, insight-service, alert-service). Match the style of the service being modified.
- **Entities:** Lombok `@Data @Builder @NoArgsConstructor @AllArgsConstructor @Entity`. ID strategy is always `GenerationType.IDENTITY`.
- **Mappers:** `@Component` classes wrapping `ModelMapper`. Pattern: `toDto()`, `toEntity()`, `updateXxxEntity(dto, entity)`. See `UserMapper`, `DeviceMapper`.
- **Services:** Concrete classes only — no service interfaces. `@Service @Slf4j @RequiredArgsConstructor`.
- **Validation messages:** user-service and device-service use `messages/messages.properties` (error messages) and `ValidationMessages.properties` (constraint messages). Message keys are referenced via `{key}` in annotations.
- **Logging:** `@Slf4j` on all service/component classes. user-service has AOP aspects that log all service method entry/exit and controller execution time.
- **Enums:** `DeviceType` (`SPEAKER`, `CAMERA`, `THERMOSTAT`, `LIGHT`, `LOCK`, `DOORBELL`) is defined in `device-service` and duplicated as a String in DTOs consumed by other services.

---

## Configuration

All configuration is in per-service `src/main/resources/application.yml`. No Spring profiles, no config server, no `.env` files.

**`@Value` is used directly** in most places. The one `@ConfigurationProperties` class is `ExcludedProperties` in `api-gateway`, which binds the `excluded.urls` list (paths that bypass JWT auth).

**Sensitive values in `application.yml` (do not commit real values to production):**
- `spring.datasource.password` — MySQL root password
- `influx.token` — InfluxDB admin token
- `keycloak.auth.jwk-set-uri` / `spring.security.oauth2.resourceserver.jwt.issuer-uri` — Keycloak endpoints

**Service URLs are hardcoded** to `localhost` in each `application.yml`. There is no service discovery (no Eureka, no Consul).

**MySQL port is `3308`** (Docker maps 3308 → 3306 inside the container). All `datasource.url` values must use port `3308` when connecting from the host.

**InfluxDB config** (`usage-service`): `influx.url`, `influx.token`, `influx.org`, `influx.bucket` — wired into `InfluxDBConfig.java`.

**Simulation config** (`ingestion-service`): `simulation.requests-per-interval`, `simulation.interval-ms`, `simulation.parallel-threads`.

---

## Database

- **MySQL 8.3** for `user-service`, `device-service`, `alert-service` — all connect to the same `home_energy_tracker` database.
- **Flyway** migrations live exclusively in `user-service/src/main/resources/db/migration/`. `user-service` must start first. `device-service` and `alert-service` set `ddl-auto: validate`.
- Migration naming: `V{n}__{description}.sql` (e.g. `V1__user_table.sql`). Add new migrations only in `user-service`.
- **InfluxDB 2.7** for `usage-service`. Measurement: `energy-usage`, tag: `deviceId`, field: `energyConsumed`. Queries use Flux query language built as format strings in `UsageService`.
- Repositories extend `JpaRepository`. Custom query example: `DeviceRepository.findAllByUserId(Long userId)`.
- No `@Transactional` annotations are present — transactions are implicit via `JpaRepository`.

---

## API & Security

**Authentication:** The `api-gateway` is the sole authentication boundary. It validates Bearer JWT tokens issued by Keycloak (`het-security-realm` on `localhost:8091`) using `spring-boot-starter-oauth2-resource-server`. The `JwtDecoder` bean is configured in `SecurityConfig` using `keycloak.auth.jwk-set-uri`. Downstream services have no `SecurityConfig` and perform no authentication — they trust that the gateway has already authenticated the caller.

**Excluded paths** (bypass JWT at the gateway, configured via `ExcludedProperties` / `excluded.urls` in `application.yml`):
- `/actuator/**`, `/swagger-ui/**`, `/v3/api-docs/**`, `/docs/**`, `/swagger-resources/**`, `/api-docs/**`, `/swagger-ui.html`

**Inter-service calls:** `RestTemplate` clients (`DeviceClient`, `UserClient`, `UsageClient`) make unauthenticated HTTP calls directly between services — no auth headers are set.

- Context path `/api/v1` is set on all services except `api-gateway`.
- **Gateway routing:** Each downstream path prefix maps to one `RouterFunction` bean in `api-gateway/route/`.
- **Validation:** `@Valid` on controller method parameters. Constraint messages use `{key}` referencing `ValidationMessages.properties`.
- **Error responses** (user-service): `ErrorResponse` record — `{ code, message, timestamp }`. Errors are mapped via `ErrorCode` enum which holds the HTTP status, string code, and i18n message key. `GlobalExceptionHandler` resolves messages via `MessageSource`.
- **device-service** has a simpler handler (`RestExceptionHandler`) returning a plain `String` body with `404` — not the `ErrorResponse` record pattern.
- **OpenAPI/Swagger:** `user-service` and `device-service` have `OpenApiConfig` beans (springdoc-openapi 3.0.2). The gateway aggregates both at `http://localhost:9000/swagger-ui.html` via `springdoc.swagger-ui.urls` config.

---

## Integrations

**Kafka (KRaft, no ZooKeeper):**
- Producer: `ingestion-service` → topic `energy-usage` (JSON, `JsonSerializer`)
- Consumer: `usage-service` listens on `energy-usage` (group `usage-service`)
- Producer: `usage-service` → topic `energy-usage-alerting`
- Consumer: `alert-service` listens on `energy-usage-alerting` (group `alert-service`)
- Event classes are duplicated per service under `com.teamengineoil.kafka.event`. Kafka type mapping is configured via `spring.json.type.mapping` in `application.yml`.
- No dead-letter topics, no retry configuration, no error handlers beyond `log.error`.

**RestTemplate (inter-service HTTP):**
- All clients are hand-rolled `@Component` classes using `RestTemplate` + `UriComponentsBuilder`. No Feign, no WebClient.
- Pattern: `DeviceClient`, `UserClient` (in `usage-service`), `UsageClient` (in `insight-service`).
- No timeout, retry, or circuit-breaker configuration on `RestTemplate` calls — failures propagate as exceptions.

**InfluxDB:** `InfluxDBClient` bean in `usage-service/config/InfluxDBConfig.java`. Writes use `WriteApiBlocking`. Reads use `QueryApi` with raw Flux query strings.

**Ollama / Spring AI:** `OllamaChatModel` injected into `InsightService`. `ChatClient` bean configured in `OllamaConfig` with a system prompt. Model is `deepseek-coder`. `pull-model-strategy: never` — model must be pulled manually before starting.

**Email:** `JavaMailSender` in `alert-service`. Uses `SimpleMailMessage`. Mailpit on `localhost:1025` in development. Every send attempt (success or failure) persists an `Alert` record to MySQL.

**Resilience4j (api-gateway only):** COUNT_BASED sliding window, size 8, 20% failure threshold, 5s open state, 2 calls in half-open. Configured in `api-gateway/src/main/resources/application.yml`. No circuit breakers on individual service `RestTemplate` calls.

**Prometheus metrics:** `user-service`, `device-service`, and `api-gateway` expose `/actuator/prometheus` via `micrometer-registry-prometheus`. Actuator endpoints exposed: `health`, `info`, `metrics`, `prometheus`.

---

## Testing

The test suite is minimal. `UserServiceApplicationTests` is the only test class and is annotated `@Disabled` — it contains a data-seeding helper, not assertions. No MockMvc, no Testcontainers, no integration test infrastructure exists.

```bash
cd user-service && ./mvnw test
```

When adding tests, use `@SpringBootTest` with `@Disabled` for integration tests that require a running database (following the existing pattern), or write unit tests with Mockito for service-layer logic.

---

## Key Files

| File | Purpose |
|---|---|
| `user-service/src/main/java/.../UserService.java` | Canonical service pattern |
| `user-service/src/main/java/.../UserMapper.java` | Canonical ModelMapper wrapper pattern |
| `user-service/src/main/java/.../exception/GlobalExceptionHandler.java` | Full error-handling pattern (ErrorCode + MessageSource) |
| `user-service/src/main/java/.../exception/ErrorCode.java` | Error code enum with HTTP status + i18n key |
| `user-service/src/main/java/.../config/OpenApiConfig.java` | Canonical OpenAPI config pattern (same structure in device-service) |
| `user-service/src/main/resources/db/migration/` | All Flyway migrations (add new ones here only) |
| `user-service/src/main/resources/messages/messages.properties` | i18n error messages |
| `usage-service/src/main/java/.../service/UsageService.java` | Kafka listener + InfluxDB write/query + scheduled alerting |
| `usage-service/src/main/java/.../client/DeviceClient.java` | Canonical RestTemplate inter-service client pattern |
| `usage-service/src/main/java/.../config/InfluxDBConfig.java` | InfluxDB client bean |
| `insight-service/src/main/java/.../config/OllamaConfig.java` | ChatClient bean with system prompt |
| `insight-service/src/main/java/.../service/InsightService.java` | Spring AI / Ollama usage pattern |
| `alert-service/src/main/java/.../service/EmailService.java` | JavaMailSender + alert persistence |
| `api-gateway/src/main/java/.../config/SecurityConfig.java` | OAuth2/JWT security config + JwtDecoder bean |
| `api-gateway/src/main/java/.../config/ExcludedProperties.java` | @ConfigurationProperties pattern for excluded URL list |
| `api-gateway/src/main/java/.../route/UserServiceRoutes.java` | Canonical gateway route + circuit breaker + fallback pattern |
| `api-gateway/src/main/resources/application.yml` | Resilience4j, Keycloak, Swagger aggregation, Prometheus config |
| `docker-compose.yml` | Full infrastructure stack |

---

## Rules for AI Coding Agents

1. **Never modify Flyway migrations.** Add a new `V{n}__description.sql` in `user-service/src/main/resources/db/migration/` instead.
2. **Never change `ddl-auto`** in `device-service` or `alert-service` from `validate` — schema is owned by `user-service`.
3. **Do not add service discovery or a config server.** Service URLs are intentionally hardcoded in `application.yml`.
4. **Do not introduce WebClient or Feign.** All inter-service HTTP calls use hand-rolled `RestTemplate` clients. Follow the pattern in `DeviceClient`.
5. **Do not add circuit breakers to individual service clients.** Resilience4j is only configured at the gateway layer.
6. **Kafka event classes must be duplicated** in each service that uses them under `com.teamengineoil.kafka.event`. There is no shared module.
7. **Match the DTO style of the service being modified** — Lombok `@Data` class in user-service/device-service; Java `record` with `@Builder` in usage-service/insight-service/alert-service.
8. **Do not add new error codes without a corresponding entry** in `messages/messages.properties` and `ErrorCode` enum (user-service pattern).
9. **Do not hardcode credentials or tokens** in source code. Use `@Value` referencing `application.yml` properties.
10. **`usage-service` route is not exposed through the gateway.** It is only called service-to-service. Do not add a gateway route for it without understanding the alerting scheduler's direct HTTP calls.
11. **`ParallelDataSimulator` is active by default** (has `@Scheduled`). `ContinuousDataSimulator` is disabled (annotation commented out). Do not re-enable `ContinuousDataSimulator` without disabling `ParallelDataSimulator` first.
12. **Do not add `SecurityConfig` to downstream services.** Authentication is enforced exclusively at the gateway via OAuth2/JWT. Downstream services are intentionally unauthenticated.
13. **Do not add OpenAPI config to services that don't already have it.** Currently only `user-service` and `device-service` have `OpenApiConfig`. The gateway aggregates their docs — adding a new service requires updating `springdoc.swagger-ui.urls` in `api-gateway/application.yml` too.
14. **Keycloak is a required runtime dependency for the gateway.** It is not in `docker-compose.yml` and must be started separately on port `8091` with the `het-security-realm` realm configured.
