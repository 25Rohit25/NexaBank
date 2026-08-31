# Nexa Bank

Nexa Bank is being built backend-first. The deterministic banking backend is now implemented: identity, accounts, atomic money movement, durable ledger and outbox, Redis idempotency, Kafka projections, notifications, audit records, and a secured API gateway.

## Technology

- Java 21
- Spring Boot 4.1.1
- Maven 3.6.3+
- PostgreSQL 17 with pgvector available for a later RAG phase
- Redis 8 and Kafka 4
- Spring Cloud Gateway MVC 5.0.3
- Spring AI 2.0.1 with Ollama and MCP

## Repository

```text
services/customer-service  Customer profile, registration, login, JWT
services/account-service   Savings/current accounts and balances
services/transaction-service  Kafka-backed transaction history projection
services/notification-service Structured transfer notification logs
services/audit-service     Durable banking event audit records
services/api-gateway       JWT validation, routing, correlation IDs
mcp/banking-mcp-server     Secured Streamable HTTP banking tools
ai/agent-service           JWT-scoped ChatClient and authenticated MCP client
infrastructure/docker      Local database initialization
docs                       Architecture and API contract
```

See [foundation architecture](docs/architecture.md) and [API reference](docs/api.md).

## Prerequisites

Install JDK 21, Maven, Git, and Docker Desktop. On Windows, verify them with:

```powershell
./scripts/check-prerequisites.ps1
```

## Start the banking backend

1. Create local configuration:

   ```powershell
   Copy-Item .env.example .env
   ```

   Replace the example values. `JWT_SECRET` must be at least 32 random bytes and identical for every HTTP service.

2. Start PostgreSQL, Redis, and Kafka:

   ```powershell
   docker compose up -d
   docker compose ps
   ```

3. Run each service in its own terminal:

   ```powershell
   mvn -pl services/customer-service spring-boot:run
   mvn -pl services/account-service spring-boot:run
   mvn -pl services/transaction-service spring-boot:run
   mvn -pl services/notification-service spring-boot:run
   mvn -pl services/audit-service spring-boot:run
   mvn -pl services/api-gateway spring-boot:run
   mvn -pl mcp/banking-mcp-server spring-boot:run
   mvn -pl ai/agent-service spring-boot:run
   ```

   The agent defaults to Ollama at `http://localhost:11434` with `qwen3:8b`. Override `OLLAMA_BASE_URL` or `OLLAMA_CHAT_MODEL` in the environment when using a different local model.

4. Verify gateway health:

   ```powershell
   curl.exe http://localhost:8080/actuator/health
   ```

The PostgreSQL initialization script creates `nexa_customer`, `nexa_account`, `nexa_transaction`, and `nexa_audit`. An old PostgreSQL volume will not rerun initialization automatically.

## End-to-end smoke test

Register a customer:

```powershell
$registration = curl.exe -s -X POST http://localhost:8080/api/v1/auth/register `
  -H 'Content-Type: application/json' `
  -d '{"firstName":"Rohit","lastName":"Singh","email":"rohit@example.com","phone":"9876543210","password":"securePassword"}' | ConvertFrom-Json
$token = $registration.accessToken
```

The JWT subject is the authoritative customer ID. Decode it locally or call the customer endpoint after obtaining the ID from the `sub` claim. Then create an account without sending any customer ID in the body:

```powershell
$account = curl.exe -s -X POST http://localhost:8080/api/v1/accounts `
  -H "Authorization: Bearer $token" `
  -H 'Content-Type: application/json' `
  -d '{"accountType":"SAVINGS","currency":"INR"}' | ConvertFrom-Json

curl.exe -s http://localhost:8080/api/v1/accounts/$($account.accountId)/balance `
  -H "Authorization: Bearer $token"
```

## Build and test

```powershell
mvn clean verify
```

The project targets Java 21. The secured MCP tool layer and the first authenticated agent workflow are implemented. Live agent verification requires the banking stack and Ollama. RAG, frontend, container images, observability, and Kubernetes remain later phases.
