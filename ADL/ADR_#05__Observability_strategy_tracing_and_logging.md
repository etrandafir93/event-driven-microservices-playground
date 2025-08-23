# ADR #05 - Observability Strategy (Tracing & Logging)

## Status
Proposed

## Context
We are building an event-driven system with three Spring Boot microservices communicating via Kafka. To debug cross-service flows and validate reliability patterns (Inbox/Outbox), we need end-to-end observability that provides:

- Correlated distributed traces across HTTP and Kafka boundaries
- Service, JVM, and business metrics 
- Structured logs correlated with traces (traceId/spanId) for efficient triage

## Decision
We will adopt a standard Spring Boot 3.x observability stack centered on Micrometer:

1. Tracing
   - Use Micrometer Tracing with Brave and Zipkin exporter (default Spring Boot 3 setup)
   - Propagation: B3 multi or W3C tracecontext (default acceptable); ensure Kafka headers carry tracing context
   - Sampling: default 100% in local/dev; 

2. Metrics
   - Use Micrometer metrics + Prometheus registry
   - Expose `/actuator/prometheus` in each service
   - Use basic Grafana dashboards for JVM, Spring, and Kafka client metrics; 

3. Logging
   - Use JSON structured logging (Logback) with fields: `timestamp`, `level`, `logger`, `message`, `traceId`, `spanId`
   - Ensure correlation by including MDC keys `traceId` and `spanId` in the log pattern

## Consequences

✅ Improved debuggability of choreograped flows via end-to-end traces (HTTP + Kafka)

✅ Faster incident triage via log–trace correlation

❌ Extra configuration per service (actuator, tracing, logback JSON)

## Measurements
We will validate this ADR by:

- Seeing end-to-end traces in Zipkin for: Order -> Kafka -> Inventory -> Kafka -> Shipping -> Kafka -> Order
- Prometheus scrape success for all services with metrics available in Grafana
- Logs include `traceId`/`spanId` and align with Zipkin trace IDs
