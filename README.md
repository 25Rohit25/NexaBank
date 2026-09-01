# Nexa Bank

Nexa Bank is a backend-first banking platform that combines deterministic Spring Boot services with a secured AI agent, MCP tools, policy RAG, a customer web application, and production engineering assets.

The governing rule is simple: **the model chooses an intent; authenticated banking services decide whether the action is allowed and execute it.**

## What is implemented

- Customer registration/login with JWT and customer-scoped authorization
- Savings/current accounts, balances, deposits, and atomic transfers
- Persistent idempotency, ledger entries, and a transactional Kafka outbox
- Kafka transaction projections, notifications, and durable audit records
- API gateway routing, JWT validation, rate boundaries, and correlation IDs
- Authenticated Streamable HTTP MCP tools with two-step transfer confirmation
- Ollama-backed agent workflows, pgvector policy RAG, and Redis conversation memory
- Deterministic AI safety/evaluation suite covering tools, grounding, authorization, and hallucination avoidance
- Responsive React/Vinext customer application
- Full Docker Compose topology with health checks
- Prometheus metrics and a provisioned Grafana dashboard
- Unit, security, MCP, RAG, agent, and Docker-aware PostgreSQL/Redis/Kafka integration tests
- GitHub Actions CI, CodeQL, dependency review, Dependabot, and GHCR image publishing
- Kubernetes workloads, persistence, probes, resource bounds, HPA, ingress, network policy, and external-secret integration

## Architecture

```text
Browser → Frontend → API Gateway
                       ├─ Customer Service → PostgreSQL
                       ├─ Account Service  → PostgreSQL + Redis → Outbox → Kafka
                       ├─ Transaction Service ← Kafka → PostgreSQL
                       └─ Agent Service → MCP Server → Gateway → Banking services
                              │
                              ├─ Redis conversation memory
                              ├─ pgvector policy retrieval
                              └─ Ollama chat + embeddings

Kafka → Notification Service
      → Audit Service → PostgreSQL

All HTTP services → Prometheus → Grafana
```

MCP is the live-data/action path. RAG is the policy-knowledge path. Neither path grants the model direct database access or authority to move money.

## Technology

- Java 21, Spring Boot 4.1.1, Spring AI 2.0.1, Maven
- PostgreSQL 17 + pgvector, Redis 8, Kafka 4
- Ollama, Streamable HTTP MCP
- React 19, TypeScript, Tailwind CSS, shadcn/ui, Vinext
- Docker Compose, Prometheus, Grafana, Kubernetes/Kustomize
- JUnit 5, Mockito, Testcontainers, GitHub Actions, CodeQL

## Repository map

```text
services/                    Deterministic banking microservices and gateway
mcp/banking-mcp-server/      Authenticated banking tools
ai/agent-service/            Agent, MCP client, RAG, and conversation memory
ai/evals/                    Golden cases and evaluation scorecards
rag/documents/               Versioned banking policy knowledge
frontend/                    Customer web application
infrastructure/docker/       Container build and PostgreSQL initialization
infrastructure/prometheus/   Metrics scraping
infrastructure/grafana/      Provisioned datasource and dashboard
infrastructure/kubernetes/   Base and production Kustomize manifests
docs/                        Architecture, APIs, MCP tools, and security
.github/                     CI, security analysis, releases, and dependency updates
```

See [architecture](docs/architecture.md), [API reference](docs/api.md), [MCP tools](docs/mcp-tools.md), and [security model](docs/security.md).

## Local quick start

Requirements: Docker Desktop with its engine running. Java 21 and Maven are required only for host-side development.

1. Create local configuration and replace the placeholder credentials:

   ```powershell
   Copy-Item .env.example .env
   ```

2. Build and start the complete stack:

   ```powershell
   docker compose up --build -d
   docker compose ps
   ```

3. Install the local models once:

   ```powershell
   docker compose exec ollama ollama pull qwen3:8b
   docker compose exec ollama ollama pull mxbai-embed-large
   ```

4. Open the services:

   - Customer app: http://localhost:3000
   - API gateway: http://localhost:8080
   - Prometheus: http://localhost:9090
   - Grafana: http://localhost:3001

The first PostgreSQL startup creates the customer, account, transaction, audit, and RAG databases. Existing volumes do not rerun initialization scripts.

## Build and test

```powershell
mvn clean verify
cd frontend
npm ci
npm run build
```

Integration tests use real PostgreSQL/pgvector, Redis, and Kafka containers when Docker is available. They skip cleanly on hosts without a Docker engine; CI runners execute them with Docker.

## Core security behavior

- JWT subject is the authoritative customer identity.
- Services reject cross-customer account access even if a request body supplies another ID.
- Transfers require an idempotency key and explicit confirmation before execution.
- The agent cannot report success unless the deterministic tool returns success.
- Policy answers must be grounded in retrieved documents; missing evidence produces an explicit unavailable answer.
- Passwords, JWT secrets, provider keys, bearer tokens, and complete account numbers must never be committed or logged.

Local placeholder defaults are development-only. Production Kubernetes uses the External Secrets Operator configuration documented in [docs/security.md](docs/security.md).

## Kubernetes

Render and inspect the complete base:

```bash
kubectl kustomize infrastructure/kubernetes/base
```

For production, first configure the External Secrets Operator and a `ClusterSecretStore` named `nexa-bank-secret-store`, then apply:

```bash
kubectl apply -k infrastructure/kubernetes/overlays/production
```

The image-release workflow publishes all application images to GHCR on a `v*` tag or manual dispatch. Production overlays should pin immutable commit-SHA image tags.

## Delivery status

Implementation is complete through the production-engineering phases. A live public rollout is intentionally environment-specific: it still requires a selected cluster/domain, registry visibility, external secret-store credentials, and an operator-approved deployment.
