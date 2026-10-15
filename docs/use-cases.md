# Banking Platform — Use Cases

**Status:** Initial functional scope  
**Project:** Small banking branch simulator  
**Purpose:** Define what the system must support without prescribing implementation details.

## Overview

Banking Platform simulates a small branch that starts operating with no customers. Over time, customers register, open accounts, manage shared ownership, move money, receive debit cards, and make purchases. An activity simulator will use the same application workflows to generate fictional banking activity.

The initial scope is organized into six functional areas. Each area may contain multiple application-level use cases; it is not necessarily a single class, endpoint, or Jira ticket.

## UC01 — Customer Management

**Goal:** Maintain the branch's customer records.

**Capabilities:** Register customers, view and search customer records, update contact details, and deactivate customers when allowed.

**Main rules:** A customer must have a unique identity reference; required information must be valid; deactivation must not make existing banking records inaccessible or erase their history.

**Acceptance criteria:** Staff can register and find a customer, update their details, and cannot register the same identity twice.

## UC02 — Account Management

**Goal:** Let existing customers open and manage bank accounts.

**Capabilities:** Open an account, view account details and balances, list customer accounts, and close eligible accounts.

**Main rules:** An account must have at least one holder; a new account starts with zero balance; an account cannot be closed while it has funds or pending operations; closed accounts remain available for historical consultation.

**Acceptance criteria:** An active customer can open an account and inspect it. Closing an ineligible account is rejected without changing its state.

## UC03 — Joint Account Holders

**Goal:** Support accounts owned by more than one customer.

**Capabilities:** Add an existing customer as a co-holder, view all holders, and remove a holder when permitted.

**Main rules:** An account must retain at least one holder; the same customer cannot be added twice; only authorized operations can change ownership.

**Acceptance criteria:** Two existing customers can share an account, both are visible as holders, and invalid holder changes are rejected.

## UC04 — Banking Operations

**Goal:** Move money safely between accounts and record financial activity.

**Capabilities:** Deposit funds, withdraw funds, transfer money between accounts, and inspect the transaction history.

**Main rules:** Amounts must be positive; withdrawals and transfers require sufficient available funds; accounts must be eligible; a transfer must not debit one account without crediting the other; repeated requests must not create duplicate financial effects.

**Acceptance criteria:** Valid operations update balances and transaction history consistently. Insufficient funds, invalid accounts, and concurrent operations cannot create inconsistent balances.

## UC05 — Debit Card Management

**Goal:** Allow a customer to use a debit card linked to an account.

**Capabilities:** Issue a card, view its status and linked account, block a card, and reactivate it when permitted.

**Main rules:** A card must belong to an eligible account; blocked cards cannot authorize new purchases; a debit card does not have a separate stored balance.

**Acceptance criteria:** An eligible account can obtain a card, and blocking it prevents subsequent purchase authorizations.

## UC06 — Card Purchases and Refunds

**Goal:** Simulate card payments, delayed settlement, and refunds.

**Capabilities:** Authorize a purchase, reserve available funds, confirm or reject its settlement, view purchases, and process eligible refunds.

**Main rules:** A purchase requires an active card and sufficient available funds; authorization and final settlement are separate states; funds must not be debited twice; refunds must refer to an existing purchase and cannot exceed the refundable amount.

**Acceptance criteria:** A purchase becomes pending after authorization, later settles through asynchronous processing, and may be partially or fully refunded. Duplicate and failed events must not corrupt balances.

## Supporting Feature — Branch Activity Simulator

**Goal:** Demonstrate the full banking workflow with fictional activity.

**Capabilities:** Gradually register fictional customers, open accounts, add co-holders, perform banking operations, issue cards, and generate purchases and refunds.

**Main rules:** The simulator reuses actual application use cases rather than changing database balances directly; activities follow account eligibility and available-funds rules; simulation data is isolated from any real or sensitive information.

**Acceptance criteria:** Starting from an empty branch, a user can run a simulation and inspect coherent customers, accounts, balances, cards, transactions, and purchases as they appear.

## Shared Engineering Requirements

- **Persistence:** PostgreSQL and Spring Data JPA, with relational mappings based on business needs.
- **Schema evolution:** Flyway migrations are introduced along with each new data model change.
- **Reliability:** Spring transactions and concurrency controls protect financial operations.
- **Messaging:** Kafka is used for asynchronous card purchase settlement and related processing. Consumers handle retries and duplicate deliveries safely.
- **Validation and security:** Validate inputs and enforce permissions in the backend.
- **Testing:** Unit tests isolate business logic with mocks; integration tests use disposable PostgreSQL and Kafka environments with controlled fictional data.
- **Delivery:** Use Git, code reviews, and Jenkins CI to validate changes.

## Suggested Delivery Order

1. Customer Management
2. Account Management
3. Joint Account Holders
4. Banking Operations
5. Debit Card Management
6. Card Purchases and Refunds (Kafka)
7. Branch Activity Simulator

Detailed endpoints, DTOs, JPA annotations, database columns, exception names, and individual test cases are defined during implementation, not in this scope document.
