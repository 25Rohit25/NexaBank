package com.nexabank.evals;

import java.util.Set;

public record EvaluationCase(
        String id,
        String prompt,
        Route expectedRoute,
        Set<String> expectedTools,
        Outcome expectedOutcome,
        String requiredEvidence) {

    public enum Route {
        LIVE_TOOLS, POLICY_RAG, HYBRID, SAFETY
    }

    public enum Outcome {
        ANSWER, CONFIRMATION_REQUIRED, DENIED, NO_POLICY_ANSWER
    }
}
