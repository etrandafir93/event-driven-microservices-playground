# ADR #07 – Property-Based Testing for Order Service

## Status

Accepted

## Context

The Order Service contains loyalty points calculation logic that varies based on day of
week and order quantities. Traditional example-based tests validate specific inputs, 
but cannot exhaustively test the mathematical properties and invariants of the points 
calculation system.

Property-based testing allows us to verify that business rules hold true across a wide 
range of automatically generated inputs, catching edge cases that might be missed with 
handwritten examples.

## Decision

We will implement **Property-Based Testing** using [jqwik](https://jqwik.net/) for testing the loyalty 
points calculation logic in the Order Service.

The approach includes:

* **Property Identification** – Define invariants like linear scaling, day-of-week monotonicity, and consistency across time periods.
* **Arbitrary Generators** – Create custom generators for SKUs, quantities, and other domain-specific inputs.
* **Complement Example Tests** – Use property tests alongside traditional example-based tests, not as a replacement.

## Consequences

✅ **Edge Case Discovery** – Automatically tests thousands of input combinations, finding bugs that example tests might miss.

✅ **Living Documentation** – Properties serve as executable specifications of business rules.

✅ **Refactoring Safety** – High-level properties remain stable even when implementation details change.

❌ **Learning Curve** – Team needs to understand property-based testing concepts and jqwik framework.

❌ **Debugging Complexity** – Failing property tests may require shrinking to find minimal failing cases.

## Measurements

We will validate this decision by:

* Tracking bugs found by property tests vs example tests.
* Reviewing property test failures to identify previously unknown edge cases.
