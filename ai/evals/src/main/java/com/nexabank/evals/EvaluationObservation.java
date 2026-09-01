package com.nexabank.evals;

import java.util.Set;

public record EvaluationObservation(
        String caseId,
        Set<String> selectedTools,
        boolean parametersCorrect,
        boolean ragGrounded,
        boolean authorizationSafe,
        boolean hallucinationFree,
        boolean workflowComplete) {
}
