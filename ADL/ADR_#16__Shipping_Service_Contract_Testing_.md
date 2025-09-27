# ADR #16 – Contract Testing Between Shipping UI and Shipping Service

## Status

Accepted

## Context

The Shipping UI service needs to communicate with the Shipping Service to retrieve shipment information for display in the user interface. 
Additionally, the UI enables operators to update the shipping order status.

This integration involves HTTP API calls where the UI service acts as a consumer of the Shipping Service's REST API.

## Decision

We will implement **Spring Cloud Contract Testing** between the Shipping UI service (consumer) and Shipping Service (provider).

The approach includes:

* **Provider Side (Shipping Service)** – Define contracts using Groovy DSL that specify expected request/response patterns for API endpoints.
* **Consumer Side (Shipping UI)** – Use stub runner to test against generated stubs from the provider contracts during unit/integration tests.
* **Contract Verification** – Automatically generate and run tests that verify the provider implements the contract correctly.

## Consequences

✅ **Early Detection of Breaking Changes** – Contract tests catch API incompatibilities during development before deployment.

✅ **Automated Verification** – Contract compliance is verified automatically in CI/CD pipelines.

✅ **Documentation as Code** – Contracts serve as living documentation of API expectations.

❌ **Additional Test Maintenance** – Contract definitions need to be maintained alongside code changes.

❌ **Learning Curve** – Teams need to understand contract testing concepts and tooling.

## Measurements

We will validate this decision by:

* Tracking reduction in integration-related production issues between UI and Service.
* Measuring time to detect API breaking changes in development vs. production.
