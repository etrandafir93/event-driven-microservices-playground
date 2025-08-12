# 📝 Homework Task: Event-Driven Microservices Playground

Hi there!  
Your task is to design and implement **three Spring Boot microservices** that communicate using **event choreography** as the primary integration pattern.

This project is a **sandbox for architecture mastery**, **event-driven design**, and **production-grade messaging patterns** (Inbox/Outbox).  
It’s also a perfect chance to create **conference/blog-worthy material** from the experience.

---

## 🎯 Goal

Design and implement an architecture where:

1. **Three independent microservices** work together through domain events (no central orchestrator)
2. Each service **publishes and consumes events** in a loosely coupled way
3. **Inbox and Outbox patterns** are implemented for reliability
4. Observability is built in from the start
5. Application is fully tested (whatever that means to you)

---

## 📦 Key Points

This is **not** just about making code run — it’s about:

- **Architecture clarity**: clear domain boundaries, no leaky abstractions
- **Loose coupling**: services know nothing about each other’s internals
- **Reliability**: events are never lost or duplicated without control
- **Observability**: trace the life of a single request/event across services
- **Storytelling**: be able to explain the why of each decision (conference-level clarity)

Think about:
- How to structure event payloads for forward compatibility
- When to use synchronous calls vs events
- How Inbox/Outbox patterns improve reliability — and their trade-offs

---

## 🧠 Functional Scenario

We’ll simulate a simple **Order Processing System**:

### **Order Service**
- Receives new orders
- Publishes `OrderCreated` events
- Listens for `OrderShipped` events to update status

### **Inventory Service**
- Listens for `OrderCreated` events
- Reserves stock & publishes `StockReserved` or `StockUnavailable`

### **Shipping Service**
- Listens for `StockReserved` events
- Prepares shipment & publishes `OrderShipped`

---

## 📐 Technical Requirements

- Java 21+
- Spring Boot 3.x
- Maven
- Spring Cloud Stream (or Kafka client)
- Testcontainers (Kafka)
- Micrometer Tracing
- PostgreSQL (for Inbox/Outbox)

---

## 📄 ADRs (Architecture Decision Records)

You’ll create one ADR per key decision:

1. **ADR #1** — “Event choreography as primary integration style”
2. **ADR #2** — “Use of Inbox pattern for idempotent event consumption”
3. **ADR #3** — “Use of Outbox pattern for reliable event publishing”
4. **ADR #4** — “Service boundaries & domain ownership”
5. **ADR #5** — “Observability strategy (tracing & logging)”
6. **ADR #6** — "Error handling"

ADR examples: https://github.com/joelparkerhenderson/architecture-decision-record

---

## 🚀 Sprints & Development Plan

### ✅ **Sprint 1**: Event-Driven Patterns Lightning Talk + Project Setup + Order Service Skeleton
- **20-minute presentation** (lightning talk):
   1. **Choreography** — pros/cons, example flow
   2. **Orchestration** — pros/cons, example flow
   3. **Inbox pattern** — when to use, trade-offs
   4. **Outbox pattern** — when to use, trade-offs
- Set up Maven parent project + modules for each service
- Implement Order Service basic CRUD & `OrderCreated` event publishing
- Implement Kafka + Outbox pattern in Order Service
- ADR #1 & ADR #3 complete
- ADR #5 complete

---

### ✅ **Sprint 2**: Inventory Service + Inbox Pattern
- Implement Inventory Service with listener for `OrderCreated` events
- Apply Inbox pattern to guarantee idempotency
- Publish `StockReserved` or `StockUnavailable`
- ADR #2 complete

---

### ✅ **Sprint 3**: Shipping Service + Full Event Flow
- Implement Shipping Service that listens for `StockReserved`
- Publish `OrderShipped` event
- Wire Order Service to consume `OrderShipped` to close the loop
- ADR #4 complete

---

### ✅ **Sprint 4**: Final Refinments & Presentation Preparation
- Prepare a conference-style slide deck (e.g. "Top 4 Architecture Patterns for Event Driven Applications")
   - Explain the architecture, trade-offs, and lessons learned
   - Use Kolbe cycle to tell the story (start with pain point)
---

