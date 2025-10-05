# ADR #09 – Snapshot Testing for Order Service Receipt Export

## Status

Accepted

## Context

The Order Service exports receipts to CSV format for external systems 
and reporting purposes. These receipts contain structured data 
including order information, line items, and calculations that 
need to be formatted correctly.

Testing CSV export functionality traditionally requires writing assertions
for each field and maintaining expected output strings, which becomes 
verbose and difficult to maintain as the format evolves.

## Decision

We will implement **Snapshot Testing** using the [java-snapshot-testing](https://github.com/origin-energy/java-snapshot-testing)
library for testing the receipt CSV export functionality.

The approach includes:

* **Snapshot Storage** – Store approved CSV output snapshots in version control alongside tests.
* **Automated Comparison** – Compare actual export output against stored snapshots during test execution.
* **Snapshot Updates** – Update snapshots when intentional changes are made to the export format.

## Consequences

✅ **Reduced Boilerplate** – Eliminates verbose field-by-field assertions in favor of snapshot comparisons.

✅ **Living Documentation** – Snapshots serve as examples of actual output format.

❌ **Review Discipline Required** – Teams must carefully review snapshot changes to avoid accepting bugs.

❌ **Version Control Noise** – Snapshot files add to repository size and commit diffs.

## Measurements

We will validate this decision by:

* Tracking time spent writing and maintaining export format tests.
