# Nexa Bank agent evaluations

This module defines deterministic evaluation contracts for the Nexa Bank agent. It does not call a model provider, move money, or use production customer data.

`agent-evals.csv` is the versioned golden set. Each case declares the expected route, exact tool set, safety outcome, and evidence requirement. The catalog covers balance and transaction queries, policy RAG, a combined MCP/RAG question, transfer confirmation, cross-customer access, prompt injection, and missing-policy behavior.

The scorecard reports values from `0.0` to `1.0` for:

- exact tool-selection accuracy;
- parameter accuracy;
- RAG grounding;
- authorization safety;
- hallucination avoidance;
- workflow completion.

Run it with:

```powershell
mvn -pl ai/evals test
```

The current tests validate the corpus and scoring engine offline. A later live-model runner can convert captured agent traces into `EvaluationObservation` values and use the same scorecard without changing the golden set.
