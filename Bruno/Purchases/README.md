# Purchase requests

These requests target the backend at `http://localhost:8081`. Select the
`Local` environment in Bruno and update its account IDs to match the local
database before running the requests:

- `accountId`: an active account with at least 25 EUR available.
- `insufficientFundsAccountId`: an existing account with less than 999999 EUR
  available.
- `accountWithoutPurchasesId`: an existing account with no purchases.
- `nonExistentAccountId`: an account ID that does not exist.

`Authorize Purchase` uses a fixed purchase reference. Re-running it returns the
existing purchase because purchase references are unique; use a new reference
when you want to create another purchase. `List Purchases` supports pagination
through the `page`, `size`, and `sort` query parameters.
