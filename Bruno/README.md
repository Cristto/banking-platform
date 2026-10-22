# Customer requests

These requests target the backend at `http://localhost:8081`.

Start the backend from `backend` with:

```powershell
& "$env:USERPROFILE\.m2\wrapper\dists\apache-maven-3.9.9\8e74001100ff70d6af083c5511fcc5ec49282d7017cde82c3698eee8fdf86698\bin\mvn.cmd" '-Dmaven.repo.local=C:\Users\Cristo\.m2\repository' '-Dmaven.test.skip=true' '-Dspring-boot.run.arguments=--server.port=8081' spring-boot:run
```

In Bruno, run `Register Customer` first; it should return `201 Created`. Run
`Duplicate Customer` afterwards to submit the same document number and email;
it should return `409 Conflict`. `Invalid Customer` has required fields missing
and a malformed email, so it should return `400 Bad Request`.

The registration request uses fixed sample data. If it has already been
registered in the database, it will return `409`; change the document number
and email in both registration and duplicate requests to use a fresh sample.

## Customer search and listing

Run `Register Customer` first so the sample customer exists. Then run:

- `List Customers` to check that the endpoint returns a customer array.
- `Search Customer by Document` to check an exact document query.
- `Search Customers by Name` to check a case-insensitive name query.

These requests include Bruno assertions for the HTTP status and response body.
