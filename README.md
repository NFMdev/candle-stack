# Candle Stack

Candle Stack is an event-driven market data playground built with Spring Boot, Kafka, Docker Compose, and a modular service layout. The current implementation focuses on generating simulated trade events, accepting them over HTTP, and publishing them to Kafka for downstream processing.

## Current status

Implemented now:

- `generator-service`: creates and runs simulation scenarios, then delivers generated trade events over HTTP
- `ingestion-service`: validates incoming trade events and publishes them to Kafka
- `compose.yaml`: starts Kafka plus the two active services for local development

Currently working:

- `processing-service`: placeholder for stream processing and persistence

Scaffolded for later:

- `query-api`: placeholder for read APIs backed by PostgreSQL

## Repository layout

- `generator-service/`
- `ingestion-service/`
- `processing-service/`
- `query-api/`

## Event flow

```text
generator-service
  -> POST /api/v1/market-events
  -> ingestion-service
  -> Kafka topic: market-trades
  -> processing-service
  -> query-api (planned)
```

## Prerequisites

- Docker and Docker Compose
- Java 25
- Optional: `mise` is configured in [mise.toml](/home/oblivion/repos/personal/candle-stack/mise.toml)

## Run the active stack

From the repository root:

```bash
docker compose up --build
```

This starts:

- Kafka on `localhost:29092`
- `generator-service` on `localhost:8080`
- `ingestion-service` on `localhost:8081`

## Run services locally without Docker

Each module has its own Maven wrapper. Typical local startup for the active services:

```bash
cd ingestion-service
./mvnw spring-boot:run
```

```bash
cd generator-service
./mvnw spring-boot:run
```

By default:

- `ingestion-service` connects to Kafka at `localhost:29092`
- `generator-service` expects you to point each scenario at the ingestion base URL you want to use

## Observability

Actuator endpoints are enabled on the active services. Examples:

- `http://localhost:8080/actuator/health`
- `http://localhost:8080/actuator/prometheus`
- `http://localhost:8081/actuator/health`
- `http://localhost:8081/actuator/prometheus`
- `http://localhost:8082/actuator/health`
- `http://localhost:8082/actuator/prometheus`

## Notes on planned modules

`query-api` already exists as Spring Boot module, but it is still scaffold. Its presence reflects the intended end-state architecture, not current end-to-end functionality.
