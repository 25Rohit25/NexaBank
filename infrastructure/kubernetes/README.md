# Nexa Bank Kubernetes

The manifests target the `nexa-bank` namespace and are assembled with Kustomize.

Before applying them, create the runtime secret without committing credentials:

```bash
kubectl apply -f infrastructure/kubernetes/base/namespace.yaml
kubectl apply -f infrastructure/kubernetes/base/secret.example.yaml
kubectl apply -k infrastructure/kubernetes/base
```

Replace every value in `secret.example.yaml` before applying it. Production clusters should source these values from an external secret manager rather than a checked-in manifest.
