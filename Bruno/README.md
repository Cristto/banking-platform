# Bruno API collections

This directory contains four Bruno collections: `Customers`, `Accounts`, `Transactions`, and `Purchases`. Import each collection folder into Bruno. The requests target the local backend at `http://localhost:8081`.

## Prerequisites

- Java 21 and the Maven wrapper included in `backend/`.
- PostgreSQL at `localhost:5433`, with database `banking_db` and credentials `banking_user` / `banking_password`.
- Kafka at `localhost:9092`.

The repository does not include a Compose file. Start PostgreSQL and Kafka separately, then run the backend from the project root:

```powershell
cd backend
./mvnw.cmd spring-boot:run
```

Flyway applies the database migrations when the application starts.

## Configure request variables

`Purchases` includes a `Local` environment with `baseUrl`, `accountId`, and sample account IDs. The `Accounts` and `Transactions` collections use variables in requests but do not include an environment file. Define the variables below in a Bruno environment and select it before running those requests.

| Collection | Variables |
|------------|-----------|
| Accounts | `customerId`, `nonExistentCustomerId`, `inactiveCustomerId`, `accountId`, `zeroBalanceAccountId`, `multiAccountCustomerId` |
| Transactions | `accountId`, `sourceAccountId`, `destinationAccountId`, `newAccountId` |
| Purchases | `baseUrl`, `accountId`, `insufficientFundsAccountId`, `accountWithoutPurchasesId`, `nonExistentAccountId` (provided by `Local`) |

Set valid IDs to records in the database. For negative cases, use IDs that do not exist or records with the required inactive, zero-balance, or insufficient-funds state. For transactions, the source and destination accounts must be different for a successful transfer, and the source account must have enough available funds.

## Suggested request flows

### Customers

1. Run `Register Customer`; it should return `201 Created`.
2. Run `Duplicate Customer` to submit the same document number and email; it should return `409 Conflict`.
3. Run `Invalid Customer`; missing required fields and a malformed email should return `400 Bad Request`.
4. Run `List Customers`, `Search Customer by Document`, and `Search Customers by Name` to check listing and search.

The registration request uses fixed sample data. If it has already been registered, it returns `409`; change the document number and email in both registration and duplicate requests to use fresh data.

### Accounts

Run `Open Account` with an existing active `customerId`, then use the returned account ID in `Get Account` and `List Customer Accounts`. `List Customer Accounts - Multiple` requires a customer with multiple accounts. The other requests cover a zero-balance account and missing or inactive customer/account cases.

### Transactions

Run `Deposit Funds` to add funds, then `Withdraw Funds` or `Transfer Funds` using accounts with suitable balances. `View Transaction History` checks the account's transactions. The remaining requests cover insufficient funds, a same-account transfer, a missing account, and an account with no transactions.

The successful deposit, withdrawal, and transfer requests use fixed idempotency keys. Use a new key when you want to exercise a fresh financial operation; reusing a key is intended to be idempotent.

### Purchases

Select the `Local` environment and set `accountId` to an account with at least 25 EUR available. Set `insufficientFundsAccountId` to an existing account with less than 999999 EUR available, and set `accountWithoutPurchasesId` to an existing account with no purchases. Run `Authorize Purchase`, then `List Purchases`. The other requests cover invalid input, insufficient funds, a missing account, and an empty purchase list.

The purchase authorization uses the fixed reference `BRU-PURCHASE-001`. Repeating it returns the existing purchase because purchase references are unique; use a fresh reference to create another purchase. `List Purchases` supports the `page`, `size`, and `sort` query parameters. Settlement and refund are Kafka events; this collection does not publish those events.

## Assertions

Bruno test assertions are included in many requests, but not every request has a `tests` block. Check the HTTP status and response body for requests without assertions.
