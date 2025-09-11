# ADR #12 - Use Pipes and Filters Architecture for Inventory Service Processing Pipeline

## Status
Accepted

## Context
The inventory service handles incoming OrderCreated events and processes them through multiple stages:
- Splitting order events into individual item reservation requests
- Attempting inventory reservation for each item
- Publishing stock reservation outcomes on stock-reserved / stock-unavailable topics
- If the stock levels goes below a threshold, it starts a stock-replenishment process

Currently, this processing is implemented using [Spring Cloud Functions](https://spring.io/projects/spring-cloud-function) 
and [Spring Cloud Stream](https://spring.io/projects/spring-cloud-stream), with individual Function components. 
However, it lacks a clear architectural pattern to manage the data flow and transformation stages.

## Decision
We will adopt the [**Pipes and Filters**](https://www.enterpriseintegrationpatterns.com/patterns/messaging/PipesAndFilters.html)
architectural pattern for the inventory service processing pipeline.

The architecture will consist of:

1. **Filters**: Individual processing components that transform, validate, or enrich data
   - `OrderCreatedListener` - Splits OrderCreated events into ItemOrdered events, 
   using the product sku as partition key.
   - `ItemReservationAttempt` - Processes individual item reservations, and returns
   a StockReserved or StockUnavailable event.
   - `ReservationOutcomePublisher` - Publishes reservation results to the stock-reserved / stock-unavailable topics.

2. **Pipes**: Data flow channels between filters implemented using Spring Cloud Stream bindings
   - Input: OrderCreated events from order service
   - Intermediate: ItemOrdered events between splitting and reservation
   - Output: Reservation outcome events to downstream services

## Consequences

✅ **Clear Data Flow**: Explicit pipeline stages make the processing flow obvious and documentable

✅ **Modularity**: Each filter can be developed, tested, and maintained independently

✅ **Extensibility**: New filters can be easily added to the pipeline (e.g., fraud detection, inventory validation)

❌ **Debugging Complexity**: Tracing issues through multiple pipeline stages can be more challenging

## Measurements

This architectural decision will be enforced through:
- ArchUnit tests ensuring proper separation of filter responsibilities
- Integration tests validating end-to-end pipeline behavior