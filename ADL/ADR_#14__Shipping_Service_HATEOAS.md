# ADR #14 - Use HATEOAS in the Shipping Service REST API

## Status
Accepted

## Context
The Shipping Service exposes REST APIs for tracking shipments, retrieving shipping information,
and managing shipment lifecycle. Clients include customer-facing applications,
partner systems, and internal services that need to interact with shipping data.

Currently, clients need to understand the available operations and construct URLs manually,
which creates tight coupling between the API and its consumers. 
This makes it difficult to evolve the API without breaking existing integrations.

## Decision
We will implement [HATEOAS](https://spring.io/projects/spring-hateoas) 
(Hypermedia as the Engine of Application State) in the Shipping Service REST API.

The API will include hypermedia links in responses that:
- Guide clients through available actions based on current shipment state
- Provide discoverable navigation paths for related resources
- Enable clients to follow links rather than constructing URLs manually

This approach will make the API self-descriptive and reduce coupling between the service 
and its clients.

## Consequences

✅ **API Discoverability**: Clients can discover available operations dynamically through hypermedia links

✅ **Loose Coupling**: Clients depend on link relations rather than hardcoded URLs, improving API evolution

✅ **State-Driven Navigation**: Available actions are contextual to the current shipment state

❌ **Response Size**: Additional hypermedia links increase payload size


## Measurements

This architectural decision will be enforced through:
- API contract tests validating presence of required hypermedia links
- Integration tests ensuring clients can navigate the API using only provided links
