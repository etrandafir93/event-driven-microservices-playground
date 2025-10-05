# ADR #08 – Mutation Testing for Inventory Service

## Status

Accepted

## Context

The Inventory Service contains critical business logic for stock replenishment, 
including threshold calculations, seasonal adjustments, and SKU-specific pricing logic.
Traditional code coverage metrics (line/branch coverage) can provide false confidence 

Mutation testing helps identify gaps in test effectiveness by introducing small changes
(mutations) to the code and verifying that tests catch these changes.

## Decision

We will implement **Mutation Testing** using [PIT (Pitest)](https://pitest.org/) 
for the Inventory Service, focusing initially on the stock replenishment module.

The approach includes:

* **Targeted Coverage** – Focus mutation testing on critical business logic 
(replenishment calculations, threshold adjustments).
* **Maven Integration** – Configure PIT as part of the build process with 
appropriate thresholds.
* **Mutation Score Targets** – Aim for high mutation coverage (80%+) on core 
business logic.

## Consequences

✅ **Test Quality Assurance** – Validates that tests actually verify business logic, 
not just execute it.

✅ **Refactoring Confidence** – Strong mutation coverage ensures safe refactoring 
of critical code.

❌ **Build Time Increase** – Mutation testing adds significant execution time to 
the test suite.

## Measurements

We will validate this decision by:

* Tracking mutation score for the replenishment module.
* Monitoring build time impact and adjusting mutation testing scope if needed.
* Reviewing mutation survivors to identify test improvements.
