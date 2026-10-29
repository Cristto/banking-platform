# Banking Platform — Use Cases

| | |
| --- | --- |
| **Project** | Small banking branch simulator |
| **Document status** | Initial functional scope |
| **Purpose** | Describe the system's expected behavior without prescribing implementation details. |

## Scope

Banking Platform models a small branch that starts with no customers. The initial scope covers customer management, account management, banking operations, and account purchases with asynchronous settlement and refunds through Kafka.

Purchases are associated directly with bank accounts. Debit cards, shared account ownership, and the branch activity simulator are outside this scope and may be considered in a later phase.

## How to read this document

The four functional areas below group the capabilities the system must provide. Each area may contain several application use cases; the numbered items are a functional checklist, not a required mapping to classes, endpoints, tickets, or pull requests. Implementation may group or split related capabilities according to business rules and maintainability. Keep simple CRUD operations free of unnecessary abstractions.

## UC01 — Customer Management

**Goal:** Maintain the branch's customer records.

**Capabilities:** Register customers and view or search customer records.

| ID | Use case | Description |
| --- | --- | --- |
| UC01.1 | Register customer | Create a customer with a unique identity reference. |
| UC01.2 | Search or list customers | Find customers by name or identity reference. |

**Business rules**

- Each customer must have a unique identity reference.
- Required customer information must be valid.

**Acceptance criteria:** Staff can register and find a customer. The system rejects registration when the identity reference is already in use.

## UC02 — Account Management

**Goal:** Let existing customers open and manage bank accounts.

**Capabilities:** Open accounts, view account details and balances, and list a customer's accounts.

| ID | Use case | Description |
| --- | --- | --- |
| UC02.1 | Open account | Open an account for an active customer with a zero balance. |
| UC02.2 | View account details | Inspect account information and current balance. |
| UC02.3 | List customer accounts | List the accounts associated with a customer. |

**Business rules**

- An account must have at least one holder.
- A newly opened account starts with a zero balance.

**Acceptance criteria:** An active customer can open an account and inspect its details and balance.

## UC03 — Banking Operations

**Goal:** Move money safely between accounts and record financial activity.

**Capabilities:** Deposit and withdraw funds, transfer money between accounts, and inspect transaction history.

| ID | Use case | Description |
| --- | --- | --- |
| UC03.1 | Deposit funds | Add funds to an eligible account. |
| UC03.2 | Withdraw funds | Withdraw funds when sufficient funds are available. |
| UC03.3 | Transfer funds | Transfer money between accounts atomically. |
| UC03.4 | View transaction history | Inspect recorded financial operations. |

**Business rules**

- Operation amounts must be positive.
- Withdrawals and transfers require sufficient available funds.
- Accounts involved in an operation must be eligible.
- A transfer must not debit one account without crediting the other.
- Repeated requests must not create duplicate financial effects.

**Acceptance criteria:** Valid operations update balances and transaction history consistently. Insufficient funds, invalid accounts, and concurrent operations do not produce inconsistent balances.

## UC04 — Purchases and Refunds

**Goal:** Authorize account purchases, settle them asynchronously, and process refunds through Kafka.

**Capabilities:** Authorize purchases, reserve available funds, confirm or reject settlements, process refunds, and view purchases.

| ID | Use case | Description |
| --- | --- | --- |
| UC04.1 | Authorize purchase | Create a pending purchase and reserve the required funds. |
| UC04.2 | Confirm settlement | Settle a pending purchase asynchronously through Kafka and record a `PURCHASE` transaction. |
| UC04.3 | Reject settlement | Reject a pending purchase through Kafka and release its reserved funds. |
| UC04.4 | View purchases | List purchases associated with a bank account. |
| UC04.5 | Refund purchase | Process a full or partial refund through Kafka and record a `REFUND` transaction. |

**Business rules**

- Purchases are linked directly to bank accounts, without cards.
- The account must be active and have sufficient available funds.
- Authorization and settlement are separate stages.
- Settlement and refund processing must be idempotent.
- Concurrent operations must preserve balance consistency.

**Acceptance criteria:** Authorization creates a pending purchase. Kafka processing later settles or rejects it. Refunds return funds to the account. Financial movements are recorded in `transactions`, and duplicate or failed events do not corrupt balances.
