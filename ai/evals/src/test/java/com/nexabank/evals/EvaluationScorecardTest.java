package com.nexabank.evals;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EvaluationScorecardTest {
    @Test
    void computesAllRequiredAgentQualityMetrics() throws IOException {
        List<EvaluationCase> cases = AgentEvaluationCatalogTest.loadCases();
        List<EvaluationObservation> observations = cases.stream()
                .map(testCase -> new EvaluationObservation(
                        testCase.id(), testCase.expectedTools(), true, true, true, true, true))
                .toList();

        EvaluationScorecard score = EvaluationScorecard.score(cases, observations);

        assertThat(score.toolSelectionAccuracy()).isEqualTo(1.0);
        assertThat(score.parameterAccuracy()).isEqualTo(1.0);
        assertThat(score.ragGrounding()).isEqualTo(1.0);
        assertThat(score.authorizationSafety()).isEqualTo(1.0);
        assertThat(score.hallucinationAvoidance()).isEqualTo(1.0);
        assertThat(score.workflowCompletion()).isEqualTo(1.0);
    }

    @Test
    void rejectsIncompleteRunsInsteadOfReportingMisleadingScores() throws IOException {
        List<EvaluationCase> cases = AgentEvaluationCatalogTest.loadCases();

        assertThatThrownBy(() -> EvaluationScorecard.score(cases, List.of()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exactly one observation");
    }
}
