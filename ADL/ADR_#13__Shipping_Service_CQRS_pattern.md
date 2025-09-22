# ADR #13 - Use CQRS Pattern in the Shipping Service

## Status
Accepted

## Context
The Shipping Service is responsible for managing the shipping lifecycle of orders,
including tracking shipment states and providing shipment status updates. 
The service needs to handle both:
- Write operations (creating shipments, updating status, tracking updates)
- Read operations (querying shipment status, generating shipping reports, customer tracking)

The read and write patterns have different performance and consistency requirements.

## Decision
We will implement the Shipping Service using the Command Query Responsibility Segregation (CQRS) pattern.

The architecture will separate:
- **Commands**: Handle shipment creation, status updates, and tracking information updates
- **Queries**: Handle shipment status lookups, tracking information retrieval, and reporting

This separation allows us to optimize each side independently for their specific requirements.

## Consequences

✅ **Performance Optimization**: Read and write models can be optimized independently for their specific use cases

✅ **Clear Separation**: Commands and queries have distinct responsibilities, improving code organization

❌ **Learning Curve**: Team needs to understand CQRS concepts and implementation patterns

## Measurements

This architectural decision will be enforced through:
- ArchUnit tests ensuring proper separation of command and query responsibilities
- Performance metrics tracking read vs write operation latencies separately
