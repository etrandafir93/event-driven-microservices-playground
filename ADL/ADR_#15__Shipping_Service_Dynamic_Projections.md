# ADR #15 - Use Dynamic Projections in the Shipping Service

## Status
Accepted

## Context
With the implementation of CQRS in the Shipping Service ([ADR #13](./ADR_%2313__Shipping_Service_CQRS_pattern.md)),
we have separated read and write models.
The query side needs to provide flexible ways to access shipping data without exposing the internal
domain entities or tightly coupling clients to our write model structure.

Different clients have varying query requirements - some need minimal tracking information,
others require detailed shipment histories, and some need aggregated reporting data.
Exposing the full domain entity would break encapsulation and create unwanted dependencies.

## Decision
We will implement [dynamic projections](https://docs.spring.io/spring-data/jpa/reference/repositories/projections.html#projection.dynamic) 
in the Shipping Service query side that:
- Transform domain entities into client-specific view models
- Keep read and write models completely decoupled

This relates directly to our CQRS implementation by ensuring the query side
maintains proper separation from the domain model.

## Consequences

✅ **Model Decoupling**: Read and write models remain independent, enabling separate evolution

✅ **Client Flexibility**: Different projections can serve specific client needs without over-fetching


## Measurements

The only entry points (public classes) to the `shipping/domain` package will be 
the `OrderShipmentQueries` and `OrderShipmentCommandHandler` interfaces.