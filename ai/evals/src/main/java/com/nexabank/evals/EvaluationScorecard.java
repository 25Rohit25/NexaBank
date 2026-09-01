package com.nexabank.evals;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public record EvaluationScorecard(
        double toolSelectionAccuracy,
        double parameterAccuracy,
        double ragGrounding,
        double authorizationSafety,
        double hallucinationAvoidance,
        double workflowCompletion) {

    public static EvaluationScorecard score(
            List<EvaluationCase> cases, List<EvaluationObservation> observations) {
        if (cases.isEmpty()) {
            throw new IllegalArgumentException("At least one evaluation case is required");
        }
        Map<String, EvaluationObservation> byCase = observations.stream()
                .collect(Collectors.toMap(EvaluationObservation::caseId, observation -> observation));
        if (byCase.size() != cases.size()
                || cases.stream().anyMatch(testCase -> !byCase.containsKey(testCase.id()))) {
            throw new IllegalArgumentException("Every evaluation case must have exactly one observation");
        }

        double toolAccuracy = cases.stream()
                .filter(testCase -> testCase.expectedTools().equals(byCase.get(testCase.id()).selectedTools()))
                .count() / (double) cases.size();
        return new EvaluationScorecard(
                toolAccuracy,
                rate(cases, byCase, EvaluationObservation::parametersCorrect),
                rate(cases, byCase, EvaluationObservation::ragGrounded),
                rate(cases, byCase, EvaluationObservation::authorizationSafe),
                rate(cases, byCase, EvaluationObservation::hallucinationFree),
                rate(cases, byCase, EvaluationObservation::workflowComplete));
    }

    private static double rate(
            List<EvaluationCase> cases,
            Map<String, EvaluationObservation> observations,
            Predicate<EvaluationObservation> passed) {
        return cases.stream()
                .map(testCase -> observations.get(testCase.id()))
                .filter(passed)
                .count() / (double) cases.size();
    }
}
