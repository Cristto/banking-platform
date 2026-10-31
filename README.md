# Banking Platform

A small banking branch simulator built with Spring Boot 4.1.1 and Java 21. The platform covers the full lifecycle of customer management, account operations, financial transactions, and card purchases with asynchronous settlement and refunds.

---

## Tech Stack

| Category | Technology |
|----------|------------|
| **Runtime** | Java 21, Spring Boot 4.1.1 |
| **Database** | PostgreSQL (Flyway migrations) |
| **ORM** | Spring Data JPA (Hibernate) |
| **Messaging** | Apache Kafka (Spring Kafka) |
| **Validation** | Jakarta Bean Validation |
| **API Testing** | Bruno (Bruno collection included) |

---

## Architecture

The project follows **Clean Architecture** with four clearly separated layers:

| Layer | Responsibility |
|-------|---------------|
| **Domain** | Models, enums, repository interfaces, domain exceptions |
| **Application** | Use cases (commands), business rules |
| **Interface Adapters** | REST controllers, DTOs, mappers, exception handlers |
| **Infrastructure** | Persistence adapters, Kafka consumers, configuration |

```
backend/src/main/java/com/banking/backend/
├── domain/              ← model, enums, repository interfaces, domain exceptions
├── application/         ← use cases and commands, grouped by feature
├── interfaceadapters/   ← REST endpoints, request/response models, mappers, exceptions; grouped by feature
└── infrastructure/
    ├── persistence/     ← Spring Data interfaces and JPA adapters
    └── kafka/           ← Kafka consumers, event models, and Kafka configuration
```

---

## Functional Scope

The platform models a small branch that starts with no customers. It covers four functional areas:

### 1. Customer Management

Register customers with a unique identity reference (document number + email) and search or list them.

| Use Case | Endpoint | Description |
|----------|----------|-------------|
| Register Customer | `POST /api/customers` | Create a customer with validated, unique identity fields. Returns `201 Created`. |
| Search Customers | `GET /api/customers?query=` | Case-insensitive search by document number, first name, or last name. Returns all customers when query is blank. |

**Business rules:** Document number and email must be unique (case-insensitive). Required fields are validated on input.

---

### 2. Account Management

Open and manage bank accounts linked to active customers.

| Use Case | Endpoint | Description |
|----------|----------|-------------|
| Open Account | `POST /api/accounts` | Opens a new account for an active customer. Generates an ES-prefixed account number. Returns `201 Created`. |
| Get Account | `GET /api/accounts/{id}` | Retrieves a single account with its current balance and status. |
| List Customer Accounts | `GET /api/accounts/customers/{customerId}` | Lists all accounts belonging to a customer. |

**Business rules:** Only active customers can open accounts. Accounts start with a zero balance.

---

### 3. Banking Operations

Move money safely between accounts with full idempotency and concurrency controls.

| Use Case | Endpoint | Description |
|----------|----------|-------------|
| Deposit Funds | `POST /api/accounts/{id}/deposits` | Add funds to an account. Idempotent via `idempotencyKey`. Returns `201 Created`. |
| Withdraw Funds | `POST /api/accounts/{id}/withdrawals` | Withdraw funds when sufficient balance exists. Idempotent. Returns `201 Created`. |
| Transfer Funds | `POST /api/accounts/transfers` | Transfer money between two accounts atomically. Validates same currency, active status, and sufficient funds. Idempotent. Returns `201 Created`. |
| View Transaction History | `GET /api/accounts/{id}/transactions?page=` | Paginated list of all transactions where the account is source or destination. |

**Business rules:** All monetary operations use `BigDecimal` for precision. Withdrawals and transfers require sufficient available funds. Repeated requests with the same `idempotencyKey` produce no duplicate financial effects.

---

### 4. Purchases and Refunds

Authorize card purchases, reserve funds, and settle or refund them asynchronously through Kafka.

| Use Case | Endpoint / Event | Description |
|----------|-----------------|-------------|
| Authorize Purchase | `POST /api/accounts/{id}/purchases` | Creates a `PENDING` purchase and reserves the required funds on the account. Returns `201 Created`. |
| Confirm Settlement | Kafka topic: `purchase.settlements` | Processes a `CONFIRM` event — moves funds from reserved to actual balance, records a `PURCHASE` transaction, sets status to `SETTLED`. |
| Reject Settlement | Kafka topic: `purchase.settlements` | Processes a `REJECT` event — releases reserved funds, sets status to `REJECTED`. |
| Refund Purchase | Kafka topic: `purchase.refunds` | Processes a refund event (full or partial). Credits the account and records a `REFUND` transaction. |
| List Purchases | `GET /api/accounts/{id}/purchases?page=` | Paginated list of purchases for an account, ordered by creation date descending. |

**Business rules:** Authorization and settlement are separate stages. Both settlement and refund processing are idempotent. Concurrent operations preserve balance consistency.

---

## Key Technical Features

### Concurrency & Consistency

| Feature | Implementation |
|---------|---------------|
| **Idempotency** | Deposits, withdrawals, transfers, purchase authorizations, settlements, and refunds all check for existing transactions by `idempotencyKey` (or `purchaseReference` / `eventId`) before processing. |
| **Pessimistic Locking** | `findByIdForUpdate()` uses `PESSIMISTIC_WRITE` locks on Account and Purchase entities for balance updates. |
| **Optimistic Locking** | Account entity uses a `@Version` field for concurrent write detection. |
| **Deadlock Prevention** | Transfers lock accounts in a deterministic order (by ID) to prevent deadlocks. |

### Financial Integrity

| Feature | Implementation |
|---------|---------------|
| **BigDecimal for Money** | Monetary values use `BigDecimal`; database columns use scale 2. Most amount columns use precision 18; `accounts.reserved_balance` uses precision 19. |
| **Reserved Balance** | Purchases reserve funds on the account (`reservedBalance`) before settlement, preventing double-spending. |
| **Atomic Operations** | Deposits, withdrawals, and transfers are single database transactions. |
| **Transaction Types** | `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`, `PURCHASE`, `SETTLEMENT`, `REFUND`. |

### API Design

| Feature | Implementation |
|---------|---------------|
| **RFC 7807 ProblemDetail** | All error responses follow the standard ProblemDetails format. |
| **Jakarta Validation** | Request DTOs use Bean Validation annotations (`@NotBlank`, `@Email`, `@NotNull`, etc.). |
| **Pagination** | Transaction and purchase lists support `page`, `size`, and `sort` parameters. |
| **Structured Error Responses** | Domain-specific exceptions map to appropriate HTTP status codes (400, 404, 409). |

### Kafka Integration

| Feature | Implementation |
|---------|---------------|
| **Inbound Consumers** | `SettlementConfirmationConsumer` (topic: `purchase.settlements`) and `RefundConfirmationConsumer` (topic: `purchase.refunds`). |
| **Retry Policy** | 3 retries with fixed 1-second backoff. Failed records are logged for operator review. |
| **Auto-commit Disabled** | Kafka auto-commit is disabled. Processing is idempotent so redelivered events do not repeat financial effects. |
| **Dedicated Factories** | Separate `ConcurrentKafkaListenerContainerFactory` per event type for proper deserialization. |

---

## Database Schema

| Table | Key Columns |
|-------|-------------|
| **customers** | `id`, `document_number` (UNIQUE), `first_name`, `last_name`, `email` (UNIQUE), `phone`, `status`, `created_at`, `updated_at` |
| **accounts** | `id`, `account_number` (UNIQUE), `balance`, `reserved_balance`, `currency` (default 'EUR'), `status`, `created_at`, `closed_at`, `version`, `customer_id` (FK) |
| **transactions** | `id`, `reference` (UNIQUE), `type`, `source_account_id` (FK), `destination_account_id` (FK), `amount`, `status`, `idempotency_key` (UNIQUE), `created_at`, `description` |
| **purchases** | `id`, `account_id` (FK), `purchase_reference` (UNIQUE), `merchant_name`, `amount`, `status`, `refunded_amount`, `settlement_transaction_id` (FK), `refund_transaction_id` (FK), `authorized_at`, `settled_at`, `rejected_at`, `created_at` |
| **outbox_events** | `id`, `event_type`, `aggregate_id`, `payload` (JSONB), `status`, `created_at`, `published_at`, `retry_count` |

11 Flyway migrations (V1–V11) evolved the schema from a basic customer/accounts model to the full purchase lifecycle with settlement and refund support.

---

## API Testing with Bruno

The `Bruno/` directory contains ready-to-use API collections organized by feature:

| Collection | Endpoints Covered |
|------------|------------------|
| **Customers** | Register Customer, Duplicate Customer (409 test), Invalid Customer (400 test), List Customers, Search by Document, Search by Name |
| **Accounts** | Open Account (success + error cases), Get Account (success + not found), List Customer Accounts (success + not found + multiple) |
| **Transactions** | Deposit Funds (success + insufficient funds), Withdraw Funds (success + insufficient funds), Transfer Funds (success, same account, insufficient funds), View Transaction History (success, empty, not found) |
| **Purchases** | Authorize Purchase (success, insufficient funds, invalid, account not found), List Purchases (success, empty, account not found) |

Many requests include assertions for HTTP status codes and response bodies; requests without a `tests` block require checking the response manually.
The Purchases collection covers the HTTP endpoints; settlement and refund events are consumed from Kafka and are covered by application-layer tests rather than Bruno requests.

---

## Testing

The application layer is covered by unit tests for all 14 use cases:

| Area | Tests |
|------|-------|
| Customer Management | `RegisterCustomerTest`, `SearchCustomersTest` |
| Account Management | `OpenAccountTest`, `GetAccountTest`, `ListCustomerAccountsTest` |
| Banking Operations | `DepositFundsTest`, `WithdrawFundsTest`, `TransferFundsTest`, `GetAccountTransactionsTest` |
| Purchases | `AuthorizePurchaseTest`, `ConfirmSettlementTest`, `RejectSettlementTest`, `RefundPurchaseTest`, `ListPurchasesTest` |

Tests follow the command pattern used in production code, verifying business rules, exception handling, and idempotency behavior.

---

## Quick Start

Prerequisites: Java 21, Maven, PostgreSQL available at `localhost:5433` with database `banking_db` and credentials `banking_user` / `banking_password`, and Kafka at `localhost:9092`. The repository does not include a Compose file; start these services separately before launching the application. Flyway applies the database migrations on startup.

```powershell
# Start the backend
cd backend
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"

```

The application runs on port **8081** by default and connects to PostgreSQL on port **5433** and Kafka on port **9092**. Bruno collections are under `Bruno/`; setup instructions and required variables are documented in [`Bruno/README.md`](Bruno/README.md). Only the Purchases collection currently includes a `Local` environment.

---

## Project Structure

```
banking-platform/
├── backend/                         ← Spring Boot application
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/banking/backend/
│       │   │   ├── domain/          ← model, enums, repository, exception
│       │   │   ├── application/     ← use cases grouped by feature
│       │   │   ├── interfaceadapters/ ← HTTP adapters grouped by feature
│       │   │   └── infrastructure/
│       │   │       ├── persistence/ ← JPA adapters
│       │   │       └── kafka/       ← consumers and Kafka configuration
│       │   └── resources/
│       │       ├── db/migration/    ← 11 Flyway migrations
│       │       ├── static/
│       │       └── templates/
│       └── test/java/               ← application use case tests
├── Bruno/                           ← API collections by feature
├── docs/
│   └── use-cases.md                ← Business requirements document
└── README.md
```
