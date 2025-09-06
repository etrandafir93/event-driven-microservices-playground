# ADR #01 – Event Choreography as Primary Integration Style

## Status

Accepted

## Context

Our system consists of multiple independent services (e.g., OrderService, InventoryService, ShippingService)
that collaborate to fulfill business processes such as order placement, fulfillment, and delivery.

Historically, these services communicated via direct REST calls or orchestration logic
within a central service. While this approach provided tight control over workflows, 
it introduced several issues:

* **Tight Coupling** – Services were aware of each other’s APIs and timing.
* **Reduced Availability** – Failures in downstream services could cascade.
* **Inflexibility** – Business processes were hard-coded and difficult to evolve.
* **Limited Autonomy** – Services had to coordinate synchronously, reducing parallelism and fault isolation.

## Decision

We will adopt **event choreography** as the primary integration style across services.

Instead of central orchestrators issuing commands to dependent services, services will:

* **Publish domain events** (e.g., `OrderPlaced`, `OrderDelivered`, `OrderShipped`) to a message broker.
* **Subscribe to relevant events** and react accordingly, updating local state or triggering follow-up actions.

This does not preclude the use of orchestration entirely (e.g., for user-facing workflows or time-bound processes), 
but choreography will be our default approach for inter-service collaboration.

## Consequences

✅ **Loose Coupling and High Autonomy** – Services communicate via asynchronous events 
without hard dependencies on each other’s APIs or availability.

✅ **Scalability and Resilience** – Systems can continue to function even if some services are down; 
failures are localized.

✅ **Natural Alignment with Domain Events** – Promotes domain-driven design by treating important business 
occurrences as first-class citizens.

❌ **Eventual Consistency** – Systems must tolerate temporary inconsistency 
and handle out-of-order or duplicate events.

❌ **Increased Complexity in Error Handling** – Without central control, each service must 
implement its own retry, deduplication, and compensation logic.

## Related Decisions

* [ADR #02 – Use Transactional Inbox Pattern](./ADR_%2302__Transactional_Inbox_Pattern.md): 
Ensures safe and reliable event consumption.
* [ADR #02 – Use Transactional Outbox Pattern](./ADR_%2303__Transactional_Outbox_Pattern.md): 
Ensures safe and reliable event publishing.

## Measurements

We will validate this decision by:

* Measuring time to onboard new downstream consumers via event subscriptions.
* Tracking reduction in inter-service REST dependencies.
* Observing service-level resilience to downstream service failures in chaos testing.
