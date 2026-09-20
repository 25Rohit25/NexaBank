# Production deployment runbook

The repository contains a deployable Kubernetes topology, but the target cluster, DNS zone, and secret-manager account belong to the operator. This runbook turns those environment-specific inputs into a repeatable release.

## Prerequisites

- A Kubernetes cluster with a default `StorageClass`
- `kubectl` and Kustomize access to the cluster
- ingress-nginx and a public load balancer
- cert-manager and a cluster issuer for TLS
- External Secrets Operator and a `ClusterSecretStore` named `nexa-bank-secret-store`
- Access from the cluster to GHCR images under `ghcr.io/25rohit25`
- A DNS name controlled by the operator

The included PostgreSQL, Redis, Kafka, Ollama, Prometheus, and Grafana workloads are appropriate for a portfolio or controlled demonstration environment. A regulated production environment should use managed, encrypted, highly available equivalents and an approved backup/restore policy.

## 1. Publish immutable images

Create and push a version tag after the `main` CI and Security workflows pass:

```bash
git tag -a v1.0.0 -m "Nexa Bank v1.0.0"
git push origin v1.0.0
```

The `Publish container images` workflow publishes every Java service and the frontend to GHCR with both `latest` and the Git commit SHA. Record the SHA and use only that immutable tag in production.

## 2. Create the production overlay

Before applying, update the production overlay with:

- each application image pinned to `ghcr.io/25rohit25/nexabank-<service>:<commit-sha>`;
- the real ingress hostname instead of `nexa-bank.local`;
- a TLS block and cert-manager issuer annotation;
- an `imagePullSecret` if the GHCR packages are private;
- storage sizes and resource limits appropriate for the cluster.

Do not edit generated resources in a live cluster. Keep environment-specific patches in a private overlay or deployment repository if they contain organization identifiers.

## 3. Provision secrets

The configured `ClusterSecretStore` must expose these remote keys:

| Remote key | Kubernetes key | Requirement |
| --- | --- | --- |
| `nexa-bank/production/postgres-password` | `POSTGRES_PASSWORD` | Random database password |
| `nexa-bank/production/jwt-secret` | `JWT_SECRET` | At least 32 random characters; rotate through a controlled rollout |
| `nexa-bank/production/grafana-admin-password` | `GRAFANA_ADMIN_PASSWORD` | Random administrator password |

Verify synchronization without printing secret values:

```bash
kubectl apply -f infrastructure/kubernetes/base/namespace.yaml
kubectl apply -f infrastructure/kubernetes/overlays/production/external-secret.yaml
kubectl -n nexa-bank wait --for=condition=Ready externalsecret/nexa-bank-secrets --timeout=120s
kubectl -n nexa-bank get secret nexa-bank-secrets
```

## 4. Render and deploy

Render before applying so policy or schema errors fail before rollout:

```bash
kubectl kustomize infrastructure/kubernetes/overlays/production > rendered-production.yaml
kubectl apply --server-side --dry-run=server -f rendered-production.yaml
kubectl apply --server-side -f rendered-production.yaml
```

`rendered-production.yaml` can expose environment metadata and should not be committed.

## 5. Verify the rollout

```bash
kubectl -n nexa-bank get pods,svc,ingress
kubectl -n nexa-bank rollout status deployment/customer-service --timeout=5m
kubectl -n nexa-bank rollout status deployment/account-service --timeout=5m
kubectl -n nexa-bank rollout status deployment/transaction-service --timeout=5m
kubectl -n nexa-bank rollout status deployment/api-gateway --timeout=5m
kubectl -n nexa-bank rollout status deployment/banking-mcp-server --timeout=5m
kubectl -n nexa-bank rollout status deployment/agent-service --timeout=10m
kubectl -n nexa-bank rollout status deployment/frontend --timeout=5m
```

Confirm the gateway health endpoint over HTTPS, then run the disposable-customer acceptance suite against it:

```powershell
.\scripts\smoke-test.ps1 -GatewayUrl "https://bank.example.com" -IncludeAgent
```

Also verify Prometheus targets, the Grafana dashboard, Kafka consumer health, persistent-volume binding, and a backup restore in a non-production namespace.

## 6. Roll back

Prefer redeploying the last known-good immutable image SHA. For an application-only rollback:

```bash
kubectl -n nexa-bank rollout undo deployment/<service-name>
kubectl -n nexa-bank rollout status deployment/<service-name> --timeout=5m
```

Never roll back a database migration blindly. Review the owning service's Flyway history and restore from a tested backup when a schema change is not backward compatible.

## Release evidence

Archive the version tag, commit SHA, successful CI and Security run URLs, rendered-manifest checksum, smoke-test output, and operator approval. These records make the demo deployment reproducible without committing credentials.
