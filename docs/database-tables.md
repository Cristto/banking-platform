# Banking Platform — Database Tables

**Status:** Initial data-model proposal  
**Scope:** Small banking branch simulation  
**Database:** PostgreSQL  
**Strategy:** Create tables progressively with Flyway migrations as each use case is implemented.

## 1. Overview

The branch starts with no customers or accounts. Customers, accounts, transactions and cards are created through the application's use cases or the activity simulator. We will use six main business tables.

| Table | Purpose | First needed |
|---|---|---|
| `customers` | Customer information | UC01 |
| `accounts` | Current accounts and balances | UC02 |
| `account_holders` | Ownership of individual and joint accounts | UC02–UC03 |
| `transactions` | Deposits, withdrawals, transfers and accounting effects of card operations | UC04 |
| `cards` | Debit cards linked to accounts | UC05 |
| `card_purchases` | Card authorizations, settlements and refunds | UC06 |

All identifiers below use `BIGINT` unless otherwise stated. Monetary amounts use `NUMERIC(18,2)` and EUR only in the first version. Use `TIMESTAMPTZ` for timestamps.

## 2. `customers` — Customer Management (UC01)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary key, generated |
| `document_number` | VARCHAR(30) | Required, unique; fictional identifier for simulations |
| `first_name` | VARCHAR(100) | Required |
| `last_name` | VARCHAR(150) | Required |
| `email` | VARCHAR(255) | Required, unique in this simplified model |
| `phone` | VARCHAR(30) | Optional |
| `status` | VARCHAR(20) | `ACTIVE`, `INACTIVE` |
| `created_at` | TIMESTAMPTZ | Required |
| `updated_at` | TIMESTAMPTZ | Required |

**Rules:** A customer may own multiple accounts. Deactivating a customer does not delete account or transaction history. Physical deletion is only allowed for disposable test data without dependencies, if implemented at all.

## 3. `accounts` — Account Management (UC02)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary key, generated |
| `account_number` | VARCHAR(34) | Required, unique; IBAN-like identifier for the simulation |
| `balance` | NUMERIC(18,2) | Required, initially `0.00` |
| `currency` | VARCHAR(3) | Required, initially `EUR` |
| `status` | VARCHAR(20) | `ACTIVE`, `BLOCKED`, `CLOSED` |
| `created_at` | TIMESTAMPTZ | Required |
| `closed_at` | TIMESTAMPTZ | Nullable |
| `version` | BIGINT | Required; optimistic locking (`@Version`) |

**Rules:** Every open account has at least one holder through `account_holders`. Closed accounts retain history. Closure requires zero balance and no pending card authorizations or other unsettled operations. Account balances must not become negative in the initial model.

## 4. `account_holders` — Individual and Joint Accounts (UC02–UC03)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary key, generated |
| `account_id` | BIGINT | Required, FK → `accounts.id` |
| `customer_id` | BIGINT | Required, FK → `customers.id` |
| `role` | VARCHAR(20) | `PRIMARY`, `JOINT` |
| `added_at` | TIMESTAMPTZ | Required |

**Constraints:** Unique (`account_id`, `customer_id`); one primary holder per account (partial unique index in PostgreSQL). Do not remove the last holder of an open account. A joint holder must already be a registered, eligible customer.

**JPA:** `AccountHolder` should be an entity with two `@ManyToOne` relationships rather than a bare `@ManyToMany`, because membership has its own role and date. From `Customer` and `Account`, both sides may expose `@OneToMany` collections.

## 5. `transactions` — Banking Operations (UC04, also UC06)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary key, generated |
| `reference` | VARCHAR(80) | Required, unique |
| `type` | VARCHAR(30) | `DEPOSIT`, `WITHDRAWAL`, `TRANSFER`, `CARD_SETTLEMENT`, `CARD_REFUND` |
| `source_account_id` | BIGINT | Nullable FK → `accounts.id` |
| `destination_account_id` | BIGINT | Nullable FK → `accounts.id` |
| `amount` | NUMERIC(18,2) | Required, greater than zero |
| `status` | VARCHAR(20) | `COMPLETED`, `FAILED` (initial scope) |
| `idempotency_key` | VARCHAR(100) | Unique when supplied |
| `created_at` | TIMESTAMPTZ | Required |
| `description` | VARCHAR(255) | Optional |

**Rules:** Deposits require a destination; withdrawals require a source; transfers require both distinct accounts. Completed transfers update both balances atomically. Card settlements debit the linked current account; refunds credit it. Persist only meaningful financial effects here; an authorized but unsettled card purchase lives in `card_purchases`. Never delete completed transaction history.

**JPA:** Two optional `@ManyToOne` account references. Inverse `@OneToMany` collections are optional and should not be loaded for account lists.

## 6. `cards` — Debit Card Management (UC05)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary key, generated |
| `account_id` | BIGINT | Required, FK → `accounts.id` |
| `customer_id` | BIGINT | Required, FK → `customers.id` (cardholder) |
| `card_reference` | VARCHAR(50) | Required, unique; safe display identifier |
| `last_four` | CHAR(4) | Optional simulated last four digits |
| `status` | VARCHAR(20) | `ACTIVE`, `BLOCKED`, `EXPIRED` |
| `issued_at` | TIMESTAMPTZ | Required |
| `expires_at` | TIMESTAMPTZ | Required |

**Rules:** Only a holder of an active account may receive a card. A card has no separate stored balance: it uses its account's available funds. Card blocking prevents new purchases. Do not store PAN, CVV or PIN in this learning project. Multiple cards per account are permitted for different holders.

**JPA:** Many cards to one account (`@ManyToOne`); account to cards (`@OneToMany`).

## 7. `card_purchases` — Purchases and Refunds (UC06)

| Column | Type | Notes |
|---|---|---|
| `id` | BIGINT | Primary key, generated |
| `card_id` | BIGINT | Required, FK → `cards.id` |
| `purchase_reference` | VARCHAR(80) | Required, unique |
| `merchant_name` | VARCHAR(150) | Required, fictitious merchant |
| `amount` | NUMERIC(18,2) | Required, greater than zero |
| `status` | VARCHAR(20) | `AUTHORIZED`, `SETTLED`, `DECLINED`, `CANCELLED` |
| `refunded_amount` | NUMERIC(18,2) | Required, initially `0.00` |
| `settlement_transaction_id` | BIGINT | Nullable, unique FK → `transactions.id` |
| `authorized_at` | TIMESTAMPTZ | Nullable |
| `settled_at` | TIMESTAMPTZ | Nullable |
| `created_at` | TIMESTAMPTZ | Required |

**Rules:** Authorization reserves funds but does not change the booked account balance. Available funds = booked balance minus active authorized card purchases for that account. Settlement creates one `CARD_SETTLEMENT` transaction, reduces the booked balance and releases the reservation in the same database transaction. A declined/cancelled authorization does not debit the account. Refunds are allowed only after settlement; each refund creates a `CARD_REFUND` transaction, and total refunds may not exceed the original settled amount. Multiple partial refunds can be found through `transactions` (a purchase reference can be included in the transaction description initially; add a proper purchase FK to transactions when implementing partial refund history).

**JPA:** Many purchases to one card (`@ManyToOne`); optional reference to the settlement transaction. A single dedicated purchase table is sufficient for the initial version.

## 8. Relationships at a glance

```text
customers 1 ── N account_holders N ── 1 accounts
customers 1 ── N cards          N ── 1 accounts
accounts  1 ── N transactions   (source and/or destination)
cards     1 ── N card_purchases
card_purchases 0..1 ── 1 transactions (settlement reference)
```

Customer-to-account is **many-to-many through `account_holders`**. There is no artificial one-to-one relationship in the initial model.

## 9. Technical table introduced with Kafka

**`outbox_events` (UC06):** `id`, `event_type`, `aggregate_id`, `payload` (`JSONB`), `status`, `created_at`, `published_at`, `retry_count`. This table allows an authorized purchase and its outgoing event to be recorded reliably in one PostgreSQL transaction. The Kafka consumer must be idempotent: duplicate messages cannot settle the same purchase twice.

This is infrastructure rather than a seventh business entity. Additional indexes and, if needed, a consumer deduplication table can be added when Kafka processing is implemented.

## 10. Creation order and migrations

1. **UC01:** `V1__create_customers.sql`.
2. **UC02:** `V2__create_accounts_and_holders.sql` (a new account always has an initial holder).
3. **UC03:** Reuse `account_holders`; only add a migration if requirements change.
4. **UC04:** `V3__create_transactions.sql`.
5. **UC05:** `V4__create_cards.sql`.
6. **UC06:** `V5__create_card_purchases.sql` and `V6__create_outbox_events.sql`.

Use Hibernate `ddl-auto: validate` and Flyway migrations for the schema. Keep unit tests isolated with mocks and use Testcontainers with a fresh PostgreSQL database for integration tests. Seed fixtures through test setup; the development simulator creates its own fictional data through application use cases.

## 11. Decisions to preserve simplicity

- One branch, one currency (EUR), one type of account (current account), and one type of card (debit).
- No virtual prepaid cards, loans, overdrafts, interest calculation, or international transfers.
- No hard deletion of financial history.
- No separate ledger table in the first iteration: `transactions` is a simplified movement history, not a full double-entry accounting ledger.
- Concurrency, authorization, idempotency and financial correctness still require integration tests even in a simulation.
