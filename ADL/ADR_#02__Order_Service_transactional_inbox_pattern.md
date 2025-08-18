# ADR #02 - Use Transactional Inbox Pattern in the Order Service for Handling Incoming Order Status Events

## Status
Proposed

## Context
The Order Service must react to external status updates, such as `order-shipped` 
and `order-delivered`, which are emitted by services like Shipping and Delivery. 
These updates are consumed from a message broker and are critical to keeping 
the order state in sync across the system.

Currently, the Order Service processes these events directly upon consumption 
from the broker and updates the order status in the PostgreSQL database. 
However, if the database transaction fails after acknowledging the message, 
or if the service crashes mid-processing, we risk losing the event entirely 
or introducing inconsistencies in order state.

Additionally, handling duplicated and out-of-order messages if very difficult
with the current setup, especially since some of the operations aren't idempotent.

## Decision
We will implement the Transactional Inbox Pattern for handling incoming order status events,
such as `order-shipped` and `order-delivered`.

Instead of processing the event directly upon consumption, we will:

- Persist the incoming event to an **inbox table** in the same database 
using a separate transactional consumer.
- Use a **separate event relay process** (polling or CDC) to pull the event 
from the inbox table and publish them as application domain events,
to be processed by the domain layer.

## Consequences

✅ **Reliable and Idempotent Processing** – Incoming messages are safely persisted 
before being processed, reducing the risk of lost or duplicated events.

✅ **Improved Fault Tolerance** – Events are not lost in case of crashes 
or database failures during processing.

✅ **Alignment with DDD & Hexagonal Architecture** – Processing logic remains within the domain, 
while message handling and durability are separated as infrastructure concerns.
(relates to [ADR #10](./ADR_%2310__Order_Service_DDD_and_Hexagonal_Architecture.md))

❌ **Increased Complexity** – Requires additional components (inbox table, 
polling logic or job scheduler).

❌ **Slightly Higher Latency** – Event handling is deferred until the inbox processor runs, 
but eventual consistency is preserved.

## Measurements

We will verify this decision with integration tests that simulate:

- Receipt of `order-shipped` and `order-delivered` events while the database is down.
- Failure during status update logic (event should remain in the inbox table 
and be retried without re-consuming from Kafka).
- Idempotent handling of duplicated and out-of-order events.