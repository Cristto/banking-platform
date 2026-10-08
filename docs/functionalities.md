# Banking Operations Platform

## Project Goal

Internal banking operations dashboard used by administrators and operators to monitor and control banking activity.

The project focuses on demonstrating Spring Boot through realistic financial workflows, persistence, transactions, concurrency, Kafka, Batch, security and advanced queries.

# Main Use Cases

## 1. List Accounts

Display existing accounts with pagination, sorting and filters.

Spring concepts:
- Spring Data JPA
- Pagination
- Specifications
- Projections

## 2. View Account Details

Display account information, balance, operational groups, alerts and recent activity.

Spring concepts:
- JPA relationships
- JOIN
- DTO mapping
- One-to-Many
- Many-to-Many

## 3. Block / Unblock Account

Allow authorized administrators to change the operational status of an account.

Spring concepts:
- Transactional update
- Spring Security
- Kafka event publication

## 4. List Transactions

Display deposits, withdrawals, transfers, reversals and fund operations.

Spring concepts:
- Pagination
- Filtering
- JPQL
- Specifications

## 5. View Transaction Details

Show the complete information and processing timeline for an operation.

Spring concepts:
- JOIN queries
- One-to-Many
- Projections

## 6. Execute Deposit

Process a deposit and update the account balance atomically.

Spring concepts:
- `@Transactional`
- JPA persistence
- Ledger entry
- Kafka event

## 7. Execute Withdrawal

Validate account state and balance before processing a withdrawal.

Spring concepts:
- `@Transactional`
- Business exceptions
- Locking
- Rollback
- Kafka event

## 8. Execute Transfer

Transfer money atomically between two accounts.

Flow:

1. Validate source account.
2. Validate destination account.
3. Validate account states.
4. Validate available balance.
5. Validate limits.
6. Protect balances from concurrent modifications.
7. Debit source account.
8. Credit destination account.
9. Register financial movements.
10. Complete transfer.
11. Publish event.

Spring concepts:
- `@Transactional`
- Multiple entity updates
- JPA locking
- Concurrency
- Rollback
- Kafka

## 9. Reverse Transfer

Create compensation movements for an eligible completed transfer.

The original financial records must remain immutable.

Spring concepts:
- Complex transaction
- Domain validation
- JPA relationships
- Kafka events

## 10. Review Operation on Hold

Allow an authorized operator to approve or reject an operation that requires manual review.

Spring concepts:
- Spring Security
- State transitions
- Transactional updates
- Kafka events

## 11. Monitor Alerts

List and inspect automatically generated operational alerts.

Examples:

- high amount
- repeated failures
- blocked account activity
- unusual transaction frequency

Spring concepts:
- Kafka Consumer
- JPA
- Filtering
- JOIN

## 12. Resolve Alert

Allow an authorized operator to mark an alert as reviewed or resolved.

Spring concepts:
- Spring Security
- JPA update
- Audit event

## 13. Monitor Kafka Events

Display financial events and their processing state.

The administrator can inspect:

- event type
- related operation
- consumer
- processing result
- retries
- failed processing
- Dead Letter Topic state

Spring concepts:
- Spring Kafka
- Consumer Groups
- Retry
- Idempotency
- Dead Letter Topics

## 14. Run Reconciliation

Execute a reconciliation process that checks financial consistency.

Checks may include:

- account balance consistency
- debit and credit matching
- incomplete transfers
- reversal consistency

Spring concepts:
- Spring Batch
- Job
- Step
- ItemReader
- ItemProcessor
- ItemWriter

## 15. Run Banking Simulation

Generate realistic banking activity using the actual application use cases.

Configuration:

- number of operations
- operations per second
- transfer percentage
- deposit percentage
- withdrawal percentage
- concurrency level
- failure rate
- high-value operation percentage

The simulator must make backend behavior visible in the dashboard.

Spring concepts:
- Concurrency
- Application use cases
- Kafka
- Metrics
- Scheduling
- Real-time updates

# Dashboard Queries

The dashboard should also include advanced read-only queries.

Examples:

- operations grouped by status
- total volume by operation type
- accounts with highest transaction volume
- accounts with most failed operations
- average transfer amount by operational group
- activity by time period

These queries should demonstrate:

- JOIN
- GROUP BY
- COUNT
- SUM
- AVG
- ORDER BY
- projections
- pagination

# Main Spring Technologies

The project should progressively use:

- Spring Web
- Spring Data JPA
- Spring Validation
- Spring Security
- Spring Kafka
- Spring Batch
- Spring Scheduler
- Spring Actuator / Micrometer
- PostgreSQL
- Testcontainers

# Suggested Implementation Order

1. Accounts
2. Transactions
3. Deposits
4. Withdrawals
5. Transfers
6. Transfer reversal
7. Operation review
8. Kafka events
9. Alerts
10. Security
11. Reconciliation
12. Simulator
13. Dashboard analytics