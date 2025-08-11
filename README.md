# Event-Driven Microservices Playground

A demonstration of event-driven microservices architecture using Kafka, PostgreSQL, and Spring Boot.

## Architecture Overview

This project implements an event-driven microservices system with three main services:

- **Order Service**: Handles order creation and status updates
- **Inventory Service**: Manages stock reservations and availability
- **Shipping Service**: Processes shipments and tracking

## Infrastructure

### Services Included
- **Kafka**: Message broker for event streaming
- **Kafka UI**: Web interface for Kafka monitoring (http://localhost:8080)
- **PostgreSQL**: Database for all microservices
- **pgAdmin**: Database management interface (http://localhost:8081)

### Getting Started

1. **Start the infrastructure:**
   ```bash
   docker-compose up -d
   ```

2. **Access the services:**
   - Kafka UI: http://localhost:8080
   - pgAdmin: http://localhost:8081 (admin@example.com / admin)
   - PostgreSQL: localhost:5432

3. **Database Configuration:**
   - Main DB: `microservices_db`
   - Service DBs: `order_db`, `inventory_db`, `shipping_db`
   - User: `admin` / Password: `password`

## Event Flow

```
Customer Order → OrderCreated → StockReserved/StockUnavailable → OrderShipped → Status Updated
```

See `event-flow.puml` for detailed PlantUML diagram.

## Development

Each microservice will have its own database and communicate via Kafka events:
- `OrderCreated`
- `StockReserved` / `StockUnavailable`
- `OrderShipped`