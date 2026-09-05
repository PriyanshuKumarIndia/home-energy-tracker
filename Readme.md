# Home Energy Tracker

A production-oriented microservices-based application for monitoring, managing, and analyzing household energy consumption.

The goal of Home Energy Tracker is to provide users with a centralized platform to track electricity usage, manage household appliances, monitor energy consumption, and derive useful insights from their energy data.

---

## Architecture

Home Energy Tracker follows a **microservices architecture**, where individual business capabilities are separated into independently deployable services.

```text
                         ┌───────────────────┐
                         │   React Frontend  │
                         └─────────┬─────────┘
                                   │
                                   ▼
                         ┌───────────────────┐
                         │    API Gateway    │
                         └─────────┬─────────┘
                                   │
                 ┌─────────────────┼─────────────────┐
                 │                 │                 │
                 ▼                 ▼                 ▼
        ┌────────────────┐ ┌────────────────┐ ┌────────────────┐
        │ User Service   │ │ Energy Service │ │ Appliance      │
        │                │ │                │ │ Service        │
        └───────┬────────┘ └───────┬────────┘ └───────┬────────┘
                │                  │                  │
                ▼                  ▼                  ▼
        ┌───────────────┐  ┌───────────────┐  ┌───────────────┐
        │ User Database │  │ Energy DB     │  │ Appliance DB  │
        └───────────────┘  └───────────────┘  └───────────────┘

                         ┌───────────────────┐
                         │ Notification      │
                         │ Service           │
                         └───────────────────┘

                         ┌───────────────────┐
                         │ Service Discovery │
                         │ / Configuration   │
                         └───────────────────┘

                         ┌───────────────────┐
                         │ Kafka / Messaging │
                         └───────────────────┘
```

> The architecture diagram above is a high-level representation. The actual services and infrastructure components are documented below.

---

## Microservices

| Service                        | Responsibility                                            |
| ------------------------------ | --------------------------------------------------------- |
| **API Gateway**                | Single entry point for client requests and routing        |
| **User Service**               | User registration, authentication, and user management    |
| **Energy Service**             | Energy consumption tracking and energy-related operations |
| **Appliance Service**          | Household appliance management and appliance energy usage |
| **Notification Service**       | Notifications and energy-related alerts                   |
| **Config / Discovery Service** | Centralized configuration and service discovery           |

Each microservice owns its business logic and data and can be developed and deployed independently.

---

## Technology Stack

### Backend

* Java 21
* Spring Boot 3.x
* Spring Web
* Spring Data JPA
* Spring Security
* Bean Validation
* Maven
* Lombok

### Microservices & Infrastructure

* Spring Cloud
* API Gateway
* Service Discovery
* Centralized Configuration
* Apache Kafka
* Docker
* Docker Compose

### Database

* PostgreSQL / MySQL
* Database-per-service architecture

### Observability

* SLF4J
* Logback
* Spring Boot Actuator
* Prometheus
* Grafana

### Frontend

* React
* JavaScript / TypeScript
* REST APIs

---

## Project Structure

```text
home-energy-tracker/
│
├── README.md
├── .gitignore
├── .env.example
├── docker-compose.yml
│
├── services/
│   │
│   ├── api-gateway/
│   ├── user-service/
│   ├── energy-service/
│   ├── appliance-service/
│   └── notification-service/
│
├── config/
│
├── infrastructure/
│   ├── prometheus/
│   ├── grafana/
│   └── docker/
│
├── docs/
│   ├── architecture/
│   ├── api/
│   └── decisions/
│
└── scripts/
```

> Update the structure above if the actual repository layout differs.

---

## Key Features

### User Management

* User registration
* User authentication
* Secure password handling
* JWT-based authentication
* Authorization
* User-specific data access

### Energy Tracking

* Record energy consumption
* Track historical consumption
* Retrieve energy usage data
* Analyze consumption patterns
* Energy usage summaries

### Appliance Management

* Add household appliances
* Update appliance information
* Remove appliances
* Track appliance energy consumption
* Associate appliances with users

### Notifications

* Energy consumption alerts
* Threshold-based notifications
* Event-driven notifications

### Microservices Communication

Services communicate using appropriate synchronous and asynchronous communication mechanisms.

* REST APIs for synchronous operations
* Apache Kafka for event-driven communication

---

## Security

The application uses Spring Security to protect secured endpoints.

Authentication is handled using JWT-based authentication.

Sensitive configuration such as:

* Database passwords
* JWT secrets
* API keys
* Kafka credentials
* Third-party credentials

must not be committed to the repository.

Sensitive environment-specific values should be provided through environment variables.

---

## Configuration

The repository provides an `.env.example` file containing the configuration variables required to run the application.

Create your local environment file:

```bash
cp .env.example .env
```

Then configure the required values.

> Never commit `.env` or any file containing real credentials.

---

## Running the Application

### Prerequisites

Make sure the following are installed:

* Java 21
* Maven
* Docker
* Docker Compose
* Git
* Node.js and npm (if running the frontend locally)

Verify the installations:

```bash
java -version
mvn -version
docker --version
docker compose version
```

---

## Running with Docker Compose

From the project root:

```bash
docker compose up -d
```

Check running containers:

```bash
docker compose ps
```

View logs:

```bash
docker compose logs -f
```

Stop the application:

```bash
docker compose down
```

To stop the application and remove associated volumes:

```bash
docker compose down -v
```

> Use `docker compose down -v` carefully because it removes Docker volumes and can delete local database data.

---

## Running Individual Services

Each microservice can also be run independently during development.

Navigate to the required service:

```bash
cd services/<service-name>
```

Run using Maven:

```bash
./mvnw spring-boot:run
```

On Windows:

```bash
mvnw.cmd spring-boot:run
```

---

## API Documentation

API documentation is provided using OpenAPI / Swagger.

Once the application is running, the Swagger UI can be accessed through the configured API Gateway or individual services.

```text
/swagger-ui/index.html
```

The exact URL depends on the service and gateway configuration.

---

## Testing

Run the complete test suite using:

```bash
mvn test
```

For an individual service:

```bash
cd services/<service-name>
mvn test
```

The project follows a layered testing strategy including:

* Unit tests
* Service-layer tests
* Controller tests
* Repository tests
* Integration tests

---

## Observability

The application is designed with production observability in mind.

### Logging

Application logging uses SLF4J with Logback.

Logs should provide useful contextual information without exposing sensitive information such as:

* Passwords
* JWT tokens
* API keys
* Database credentials
* Personally sensitive information

### Health Checks

Spring Boot Actuator provides application health and operational endpoints.

Example:

```text
/actuator/health
```

### Metrics

Prometheus-compatible metrics can be exposed through Spring Boot Actuator.

Grafana can then be used to visualize application and infrastructure metrics.

---

## Event-Driven Architecture

Apache Kafka is used for asynchronous communication between services where appropriate.

Example event flow:

```text
Energy Service
      │
      │ EnergyConsumptionRecorded
      ▼
   Kafka Topic
      │
      ├───────────────► Notification Service
      │
      └───────────────► Analytics / Future Services
```

This allows additional consumers to be introduced without tightly coupling them to the producer service.

---

## Database Architecture

The project follows the **database-per-service** principle.

```text
User Service
     │
     ▼
 User Database

Energy Service
     │
     ▼
Energy Database

Appliance Service
     │
     ▼
Appliance Database
```

A service should not directly access another service's database.

Cross-service data access should happen through APIs or asynchronous events.

---

## Development Principles

The project follows some software engineering principles:

* Separation of concerns
* Single responsibility
* Database per service
* Loose coupling
* High cohesion
* API-first development
* Centralized configuration
* Service discovery
* Stateless authentication
* Event-driven communication where appropriate
* Centralized exception handling
* Input validation
* Structured logging
* Automated testing
* Containerized deployment

---

## Git Workflow

Feature development should use feature branches.

Example:

```bash
git checkout -b feature/energy-consumption
```

Commit changes using descriptive commit messages:

```text
feat: add energy consumption service
feat: implement JWT authentication
fix: handle invalid energy readings
refactor: improve global exception handling
test: add energy service unit tests
docs: update architecture documentation
chore: configure docker compose
```

---

## Roadmap

### Phase 1 — Core Services

* [x] Project setup
* [ ] User Service
* [ ] Energy Service
* [ ] Appliance Service
* [ ] Notification Service
* [ ] API Gateway

### Phase 2 — Security

* [ ] User authentication
* [ ] JWT authentication
* [ ] Authorization
* [ ] Secure service communication

### Phase 3 — Distributed Systems

* [ ] Service discovery
* [ ] Centralized configuration
* [ ] Kafka integration
* [ ] Event-driven communication

### Phase 4 — Production Readiness

* [ ] Global exception handling
* [ ] Validation
* [ ] Structured logging
* [ ] Actuator
* [ ] Metrics
* [ ] Prometheus
* [ ] Grafana
* [ ] Distributed tracing

### Phase 5 — Deployment

* [ ] Dockerize all services
* [ ] Docker Compose
* [ ] CI/CD pipeline
* [ ] Cloud deployment
* [ ] Production configuration

---

## Contributing

1. Fork the repository.
2. Create a feature branch.

```bash
git checkout -b feature/my-feature
```

3. Make your changes.
4. Add or update tests.
5. Commit your changes.

```bash
git commit -m "feat: implement my feature"
```

6. Push the branch.

```bash
git push origin feature/my-feature
```

7. Open a Pull Request.

---

## License

This project is licensed under the MIT License.

See the `LICENSE` file for details.

---

## Author

**Priyanshu Kumar**

Home Energy Tracker — Microservices-based energy management platform.
