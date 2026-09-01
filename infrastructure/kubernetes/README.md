# Nexa Bank Kubernetes

The manifests target the `nexa-bank` namespace and are assembled with Kustomize.

Application images are published to GitHub Container Registry by the `Publish container images` workflow on version tags or a manual run. The Kubernetes base references the corresponding `latest` images; production releases should pin immutable commit-SHA tags in an environment overlay.

Before applying them, create the runtime secret without committing credentials:

```bash
kubectl apply -f infrastructure/kubernetes/base/namespace.yaml
kubectl apply -f infrastructure/kubernetes/base/secret.example.yaml
kubectl apply -k infrastructure/kubernetes/base
```

Replace every value in `secret.example.yaml` before applying it. Production clusters should source these values from an external secret manager rather than a checked-in manifest.
