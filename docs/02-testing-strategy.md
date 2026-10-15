# Banking Platform — Testing and Database Strategy

**Version:** 1.0  
**Objective:** Test business behavior thoroughly without polluting or depending on the developer's everyday PostgreSQL data.

## 1. Important distinction: unit tests do not mock a database

In **unit tests**, mock the persistence and messaging *dependencies* (repository ports, clock, ID generator, publisher) using Mockito. The use case itself and its rules run in memory. No actual PostgreSQL process is involved.

In **integration tests**, use **a real disposable PostgreSQL instance** through Testcontainers, run Flyway migrations and insert a small known fixture. Query the real tables to verify mappings, constraints, transactions and SQL behavior. For Kafka workflows, start a Kafka container as well. Do not use the everyday `banking_db` as a test database.

| Test type | Dependencies | What it checks | Typical tools |
|---|---|---|---|
| Domain unit | None | Pure financial/status rules | JUnit, AssertJ |
| Application unit | Mock repository/ports | Use-case decisions and orchestration | JUnit, Mockito |
| Web slice | Mock application use cases | JSON, request validation, exception mapping | `@WebMvcTest`, MockMvc |
| Repository integration | Real ephemeral PostgreSQL | JPA relations, constraints, queries | Testcontainers, Spring Data JPA |
| Application integration | Real ephemeral PostgreSQL | Commit, rollback, locks, concurrency | `@SpringBootTest`, Testcontainers |
| Kafka integration | PostgreSQL + Kafka containers | Publish, consume, retries, deduplication | Spring Kafka, Testcontainers |

## 2. Database safety rules

1. **No tests against an everyday or shared database.** Development records may be useful, but tests must be independent of them.
2. Use isolated containers/database names and a dedicated `test` profile; test credentials must not point to any real environment.
3. Apply exactly the same Flyway migrations to test DB as to local development.
4. Each test inserts only the fictional data it needs. Use `@Sql` or fixture builders/factories. Avoid depending on insertion order or hardcoded IDs from other tests.
5. Tests must be repeatable in any order. For concurrent tests, do not wrap the entire test in a transaction that hides uncommitted setup data from worker threads.
6. Use `@Transactional` on application services where atomic updates are needed; don't assume it solves idempotency or concurrent races on its own.
7. Store only fictional names and identifiers in test/seed fixtures; never commit real card data or secrets.

## 3. Practical testing targets per use case

| Use case | Unit checks | Integration checks |
|---|---|---|
| UC01 Customers | Invalid data, duplicate checks, deactivate rules | Unique indexes, CRUD, filters and pagination |
| UC02 Accounts | Open/close eligibility | Account and primary holder persisted together; rollback |
| UC03 Joint holders | Duplicate/not removable ownership | Relation query both ways; no unintended cascade deletion |
| UC04 Transfers | Funds checks, same account, idempotency decision | Concurrent updates, lock behavior, rollback and no double debit |
| UC05 Cards | Issue/block rules | Card/account foreign key and state persistence |
| UC06 Purchases | Authorized → settled/refund state machine | Kafka delivery, transaction commit, redelivery, hold and refund consistency |

## 4. Suggested project layout

```text
banking-platform/
├── backend/
│   ├── src/main/java/...
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/
│   │       ├── V1__create_customers.sql
│   │       ├── V2__create_accounts_and_holders.sql
│   │       ├── V3__create_transactions.sql
│   │       ├── V4__create_cards.sql
│   │       └── V5__create_purchases_and_outbox.sql
│   ├── src/test/java/...
│   └── src/test/resources/application-test.yml
├── docs/
│   ├── 01-use-cases.md
│   └── 02-testing-strategy.md
└── README.md
```

The migration filenames above show the intended progression, **not SQL files already implemented**.

## 5. Delivery workflow

For each operation: write business rules and acceptance examples → implement the migration if required → implement domain/application code → unit tests with mocked ports → integration tests with isolated DB → HTTP test with Bruno/MockMvc → Jenkins pipeline → merge.

**Definition of done:** normal path, at least one business rejection, persistence correctness and relevant concurrency/retry cases covered. The full simulator is built only after the operations work individually.
