# ADR #11 - Use KafkaOperations Interface and SystemTime Component for Better Testability

## Status
Proposed

## Context
Our microservices architecture relies heavily on message-driven communication through Apache Kafka
and time-sensitive operations throughout the application. Direct usage of concrete implementations 
such as KafkaTemplate, KafkaProducer, and static time methods (LocalDateTime.now(), Instant.now(), etc.)
creates tight coupling that significantly hampers testability and maintainability.

## Decision
We will enforce the use of abstraction layers for both Kafka operations and time management:

1. **KafkaOperations Interface**: All Kafka message publishing must go through the KafkaOperations 
   interface rather than directly using KafkaTemplate or KafkaProducer implementations.

2. **SystemTime Component**: All time-related operations must use our dedicated SystemTime component
   instead of static methods like LocalDateTime.now(), OffsetDateTime.now(), Instant.now(), or LocalDate.now().

## Consequences

During testing, we'll use custom components that are decorating the actual bean. 
By default, they will delegate the call to the underlying component, but, when needed, 
they can capture the function invocation and stub the response.

✅ **Improved Testability**: Easy mocking and stubbing of Kafka operations and time operations

❌ **Additional Abstraction Layer**: Slightly increased complexity in simple scenarios

## Measurements

These decisions will be enforced through ArchUnit tests to prevent architectural drift. 
