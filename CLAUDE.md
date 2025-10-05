# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Event-driven microservices playground demonstrating event choreography patterns with Spring Boot 3.x and Java 21. Three services communicate via Kafka implementing production-grade Inbox/Outbox patterns.

**Services**:
- **Order Service** (8081): Creates orders, publishes `OrderCreated`, consumes `OrderShipped`/`OrderDelivered`
- **Inventory Service** (8082): Consumes `OrderCreated`, publishes `StockReserved`/`StockUnavailable`
- **Shipping Service** (8083): Consumes `StockReserved`, publishes `OrderShipped`/`OrderDelivered`
- **Shipping UI** (8080): Vaadin UI for shipping management

## Commands

### Build & Test
```bash
# Build all modules
mvn clean install

# Build single service
cd <service-name> && mvn clean install

# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=OrderServiceTest

# Run specific test method
mvn test -Dtest=OrderServiceTest#testCreateOrder

# Skip tests
mvn clean install -DskipTests
```

### Running Locally
```bash
# Start infrastructure (Kafka, PostgreSQL, observability stack)
docker-compose up -d

# Run individual services (from service directories)
mvn spring-boot:run
```

### Observability
- Zipkin: http://localhost:9411 (tracing)
- Grafana: http://localhost:3000 (metrics, admin/admin)
- Kibana: http://localhost:5601 (logs)
- Prometheus: http://localhost:9090 (metrics)
- Kafka UI: http://localhost:8090 (topics/consumers)

## Architecture by Service

### Order Service - DDD & Hexagonal Architecture
**Structure**: `domain/` → `application/` → `infra/`

- Domain layer is isolated from infrastructure (enforced by ArchUnit)
- Ports: `ProductCatalog`, `CustomerRelationshipManagement` (interfaces in domain)
- Adapters: `ProductCatalogClient`, `CrmClient`, `OrderCreatedProducer` (implementations in infra)
- **Inbox Pattern** (`infra/inbox/`): Idempotent event consumption via PostgreSQL deduplication
- **Outbox Pattern** (`infra/outbox/`): Transactional event publishing via relay pattern
- **Snapshot Testing**: Uses java-snapshot-testing library for regression tests

Key files:
- `domain/order/OrderService.java`: Core business logic
- `infra/outbox/OutboxRelay.java`: Publishes events transactionally
- `infra/inbox/InboxProcessor.java`: Processes incoming events idempotently

### Inventory Service - Pipes & Filters Architecture
**Uses Spring Cloud Stream for functional-style event processing**

Processing pipeline:
1. `OrderCreatedListener` → splits events into individual item reservations
2. `StockReservation` → attempts reservation for each item
3. `ReservationOutcomePublisher` → publishes outcomes to Kafka

Additional:
- `replenishment/`: Auto-replenishment when stock falls below threshold
- WireMock for external supplier API mocking (port 9999)

### Shipping Service - CQRS Pattern
**Separates commands and queries with Spring Modulith**

- **Commands**: `OrderShipmentsCommandHandler` (write operations)
- **Queries**: `OrderShipmentQueries` (read operations)
- **HATEOAS**: `OrderShipmentLinks` provides hypermedia navigation
- **Dynamic Projections**: Flexible query response shapes
- **Spring Cloud Contract**: Contract tests in `src/test/resources/contracts/`
  - Base class: `io.github.etr.playground.shipping.infra.ContractTest`

## Key Technical Patterns

### Inbox Pattern (Order Service)
Prevents duplicate event processing:
- Stores consumed event IDs in PostgreSQL `inbox` table
- `InboxRelay` polls and processes events
- Configured via `InboxProperties`

### Outbox Pattern (Order Service)
Ensures reliable event publishing:
- Domain events saved to `outbox` table within same transaction
- `OutboxRelay` publishes events asynchronously
- Prevents event loss during failures

### Testability
- `SystemTime`: Injectable time abstraction (Order Service) - use in tests to control time
- `KafkaOperations`: Wrapper for Kafka operations enabling test mocking
- All services use Testcontainers (PostgreSQL, Kafka)
- ArchUnit tests enforce architectural boundaries

## Tech Stack
- **Java**: 21
- **Spring Boot**: 3.5.4
- **Spring Cloud**: 2024.0.0
- **Spring Modulith**: 1.3.1 (Shipping Service)
- **Testcontainers**: 1.21.3
- **Database**: PostgreSQL (separate DB per service)
- **Messaging**: Kafka

## ADRs
See `ADL/` directory for Architecture Decision Records covering:
- Event choreography (#1)
- Inbox pattern (#2)
- Outbox pattern (#3)
- Observability (#5)
- DDD/Hexagonal (#10)
- Pipes & Filters (#12)
- CQRS (#13)
- HATEOAS (#14)
- Contract Testing (#16)