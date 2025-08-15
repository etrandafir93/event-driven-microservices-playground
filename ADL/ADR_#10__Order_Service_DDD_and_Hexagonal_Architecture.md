# Title

## Status
Proposed

## Context
The Order Service is a core component of our e-commerce platform, 
responsible for handling the complete lifecycle of customer orders. 
In addition to maintaining **order** data, it depends on several external systems 
to enrich and process this information with **customer** and **product** details. 

This includes:
- retrieving customer and product details from external REST APIs, 
- exchanging messages via message brokers, 
- leveraging distributed caching for performance, 
and persisting data in our PostgreSQL database.

## Decision
We will implement the Order Service using Domain-Driven Design (DDD) principles
and Hexagonal Architecture.

We will decouple the core domain from the infrastructure layer through clear boundaries,
and ensure the correct source code dependency.

## Consequences

- Increased complexity vs. maintainability – Applying DDD and Hexagonal Architecture 
introduces more layers and abstractions, which adds to the initial complexity, 
but provides better separation of concerns and easier long-term maintenance.

- Alternative approaches (such as Pipeline-Oriented or Data-Oriented Design) 
may require fewer layers and abstractions, but they won't allow us to model the domain 
effectively and decouple it from the infrastructure.

## Measurements

We'll use ArchUnit to enforce these architectural constraints for the Order Service:
- The Domain layer does not depend on the Infrastructure layer,
- The Domain model is encapsulated - therefore we don't expose it directly to the outside world,
- The app uses Port and Adapter components to interact with external systems,
