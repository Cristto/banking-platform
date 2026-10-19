# UC01.1 — Register customer

`POST /api/customers`

```json
{
  "documentNumber": "SIM-0001",
  "firstName": "Alex",
  "lastName": "Example",
  "email": "alex@example.com",
  "phone": "+34 600 000 000"
}
```

Document number, first name, last name and email are required. Phone is optional.
Text is trimmed; document numbers are stored in uppercase and email addresses in
lowercase. Limits follow the existing customer schema: 30, 100, 150, 255 and 30
characters, respectively. New customers have `ACTIVE` status.

Responses:

- `201 Created`: customer data, including generated ID, status and timestamps.
- `400 Bad Request`: invalid fields or malformed JSON, using Problem Details.
- `409 Conflict`: document number or email already registered, using Problem Details.

The application use case owns the transaction and uses the domain repository port.
The JPA adapter translates unique constraint violations, including concurrent
registrations. The API returns a response DTO rather than the persistence entity.
Existing JPA mappings in the domain model are preserved.

Migration V7 adds normalized unique indexes without modifying applied migrations.
Existing records with conflicting normalized document numbers or emails must be
resolved before applying this migration.
