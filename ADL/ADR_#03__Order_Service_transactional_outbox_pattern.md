# ADR #03 - Use Transactional Outbox Pattern in the Order Service

## Status
Proposed

## Context
The Order Service emits domain events such as `order.created` to notify 
other parts of the system (e.g., inventory, shipping, billing) that an order has been placed. 
These events are sent via a message broker to external consumers.

Currently, the Order Service writes order data to a PostgreSQL database 
and publishes the `order.created` event separately. 
This introduces a risk of inconsistency if the event is published but the transaction fails (or vice versa). 
This could result in downstream systems acting on events for orders that don't exist in the database.

## Decision
We will implement the **Transactional Outbox Pattern** for emitting `order.created` events.

Instead of publishing the event directly, we will:
- Write the event to an **outbox table** in the same database transaction 
that persists the order,
- Use a **separate event relay process** (polling or CDC) to publish 
the event to the message broker.

This ensures atomicity between the database write and the event emission.

## Consequences

✅ **Guaranteed (Eventual) Consistency** – Ensures that either both the order 
and the event are persisted, or neither are. 

✅ **Alignment with DDD & Hexagonal Architecture** – Events are produced from 
within the domain layer but published asynchronously, respecting the boundaries
and separation of concerns. 
(relates to [ADR #10](./ADR_%2310__Order_Service_DDD_and_Hexagonal_Architecture.md))

❌ **Slightly Higher Latency** – Event handling is deferred until the inbox processor runs,
but eventual consistency is preserved.

❌ The order and exactly once semantics of the outgoing messages will not be guaranteed

## Measurements

We’ll enforce this decisions through an e2e tests where we send POST requests 
to create an order while Kafka is down. Later, we spin up the Kafka container 
and assert that the order-created events are eventually publish to the topic.