# Candle Stack

Candle Stack is an event-driven market data playground built with Spring Boot, Kafka, Docker Compose, and a modular service layout. The current implementation focuses on generating simulated trade events, accepting them over HTTP, and publishing them to Kafka for downstream processing.

## Current status

Implemented now:

- `generator-service`: creates and runs simulation scenarios, then delivers generated trade events over HTTP
- `ingestion-service`: validates incoming trade events and publishes them to Kafka
- `processing-service`: consumes Kafka trade events, maintains one daily snapshot per instrument, and persists snapshots to PostgreSQL
- `compose.yaml`: starts Kafka, PostgreSQL, and all three pipeline services for local development

Processing currently normalizes and validates trade events, ignores duplicate and out-of-order updates, tracks last trade, daily volume, high/low, and count, and reloads persisted snapshots on startup before consuming Kafka messages.

It does not yet build time-bucketed OHLC candles or expose a read API. The current persisted model is an instrument-level daily snapshot.

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
- PostgreSQL on `localhost:5432`
- `generator-service` on `localhost:8080`
- `ingestion-service` on `localhost:8081`
- `processing-service` on `localhost:8082`

## Run services locally without Docker

Start Kafka and PostgreSQL first (for example, with Docker Compose), then run the services locally:

```bash
mvn -pl ingestion-service spring-boot:run
```

```bash
mvn -pl generator-service spring-boot:run
```

```bash
mvn -pl processing-service spring-boot:run
```

By default:

- `ingestion-service` connects to Kafka at `localhost:29092`
- `generator-service` expects you to point each scenario at the ingestion base URL you want to use
- `processing-service` connects to Kafka at `localhost:29092` and PostgreSQL at `localhost:5432` (database `candlestack`)

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
