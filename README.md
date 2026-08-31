# Nexa Bank

Nexa Bank is being built backend-first. This repository currently implements the agreed foundation only: local infrastructure, Customer Service, JWT authentication, Account Service, service-to-service customer validation, migrations, and tests.

## Technology

- Java 21
- Spring Boot 4.1.1
- Maven 3.6.3+
- PostgreSQL 17 with pgvector available for a later RAG phase
- Redis 8 and Kafka 4 (running now, integrated in later phases)

## Repository

```text
services/customer-service  Customer profile, registration, login, JWT
services/account-service   Savings/current accounts and balances
infrastructure/docker      Local database initialization
docs                       Architecture and API contract
```

See [foundation architecture](docs/architecture.md) and [API reference](docs/api.md).

## Prerequisites

Install JDK 21, Maven, Git, and Docker Desktop. On Windows, verify them with:

```powershell
./scripts/check-prerequisites.ps1
```

## Start the foundation

1. Create local configuration:

   ```powershell
   Copy-Item .env.example .env
   ```

   Replace the example values. `JWT_SECRET` must be at least 32 random bytes and must be identical for both services.

2. Start PostgreSQL, Redis, and Kafka:

   ```powershell
   docker compose up -d
   docker compose ps
   ```

3. Run Customer Service in terminal one:

   ```powershell
   mvn -pl services/customer-service spring-boot:run
   ```

4. Run Account Service in terminal two:

   ```powershell
   mvn -pl services/account-service spring-boot:run
   ```

5. Verify health:

   ```powershell
   curl.exe http://localhost:8081/actuator/health
   curl.exe http://localhost:8082/actuator/health
   ```

The PostgreSQL initialization script creates separate `nexa_customer` and `nexa_account` databases. If a pre-existing named volume was created before these databases were added, recreate that development volume explicitly before first use.

## End-to-end smoke test

Register a customer:

```powershell
$registration = curl.exe -s -X POST http://localhost:8081/api/v1/auth/register `
  -H 'Content-Type: application/json' `
  -d '{"firstName":"Rohit","lastName":"Singh","email":"rohit@example.com","phone":"9876543210","password":"securePassword"}' | ConvertFrom-Json
$token = $registration.accessToken
```

The JWT subject is the authoritative customer ID. Decode it locally or call the customer endpoint after obtaining the ID from the `sub` claim. Then create an account without sending any customer ID in the body:

```powershell
$account = curl.exe -s -X POST http://localhost:8082/api/v1/accounts `
  -H "Authorization: Bearer $token" `
  -H 'Content-Type: application/json' `
  -d '{"accountType":"SAVINGS","currency":"INR"}' | ConvertFrom-Json

curl.exe -s http://localhost:8082/api/v1/accounts/$($account.accountId)/balance `
  -H "Authorization: Bearer $token"
```

## Build and test

```powershell
mvn clean verify
```

The project targets Java 21. No AI, MCP, RAG, frontend, or Kubernetes code is included yet; those layers follow only after this banking foundation is running reliably.
