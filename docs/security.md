# Security model

Nexa Bank keeps the language model outside the authorization boundary. JWT-authenticated Spring services validate customer ownership and business rules; MCP forwards the caller token but cannot bypass those checks. Transfers require explicit confirmation and deterministic backend success before the agent can report completion.

## Secrets

- Local development reads `.env`, which is ignored by Git. Only placeholder `.env.example` files are committed.
- Kubernetes production uses the External Secrets Operator. Create a `ClusterSecretStore` named `nexa-bank-secret-store` and populate the remote keys referenced by the production overlay.
- Rotate the PostgreSQL, JWT, and Grafana credentials before a production rollout. JWT secrets must contain at least 32 random bytes.
- Never log passwords, bearer tokens, complete account numbers, confirmation tokens, or model-provider API keys.

Apply the production overlay only after the secret store is ready:

```bash
kubectl apply -k infrastructure/kubernetes/overlays/production
```

The namespace enforces the Kubernetes baseline Pod Security Standard, audits/warns against restricted-policy violations, and denies unsolicited ingress except from Nexa Bank workloads and the ingress controller.
