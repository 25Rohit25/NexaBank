# Nexa Bank foundation architecture

This first delivery deliberately stops at the banking foundation.

```text
Client
  │
  ├── register/login ──> Customer Service (8081) ──> nexa_customer
  │                         │
  │                         └── signs JWT (sub = customer ID)
  │
  └── Bearer JWT ─────> Account Service (8082) ───> nexa_account
                            │
                            └── validates customer over HTTP
                                      │
                                      └── Customer Service

Local infrastructure: PostgreSQL + Redis + Kafka
```

## Trust boundaries

- Customer identity comes from the verified JWT `sub` claim, not request JSON.
- Account Service has no access to the customer schema. It forwards the bearer token to Customer Service when validating account creation.
- Customer and account reads enforce owner-or-admin authorization.
- Passwords are BCrypt hashes. JWT secrets are supplied through environment variables outside local development.
- Balance is `BigDecimal`/`NUMERIC(19,2)` and starts at zero; money movement is intentionally deferred to the transaction phase.

## Deliberately deferred

Transfers, deposits, Redis use cases, Kafka events, gateway, MCP, agent, RAG, frontend, observability, and Kubernetes belong to later phases.

