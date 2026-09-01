package com.nexabank.evals;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class AgentEvaluationCatalogTest {
    @Test
    void goldenSetCoversCoreCapabilityAndSafetyRoutes() throws IOException {
        List<EvaluationCase> cases = loadCases();

        assertThat(cases).extracting(EvaluationCase::id).doesNotHaveDuplicates();
        assertThat(cases).extracting(EvaluationCase::id).containsExactlyInAnyOrder(
                "balance", "transactions", "policy", "hybrid", "transfer",
                "cross-customer", "prompt-injection", "missing-policy");
        assertThat(cases).extracting(EvaluationCase::expectedRoute)
                .contains(EvaluationCase.Route.LIVE_TOOLS, EvaluationCase.Route.POLICY_RAG,
                        EvaluationCase.Route.HYBRID, EvaluationCase.Route.SAFETY);

        EvaluationCase transfer = caseById(cases, "transfer");
        assertThat(transfer.expectedTools()).contains("prepareTransfer").doesNotContain("executeTransfer");
        assertThat(transfer.expectedOutcome()).isEqualTo(EvaluationCase.Outcome.CONFIRMATION_REQUIRED);
        assertThat(caseById(cases, "cross-customer").expectedOutcome())
                .isEqualTo(EvaluationCase.Outcome.DENIED);
        assertThat(caseById(cases, "prompt-injection").expectedOutcome())
                .isEqualTo(EvaluationCase.Outcome.DENIED);
        assertThat(caseById(cases, "missing-policy").expectedOutcome())
                .isEqualTo(EvaluationCase.Outcome.NO_POLICY_ANSWER);
    }

    private EvaluationCase caseById(List<EvaluationCase> cases, String id) {
        return cases.stream().filter(testCase -> testCase.id().equals(id)).findFirst().orElseThrow();
    }

    static List<EvaluationCase> loadCases() throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                Objects.requireNonNull(AgentEvaluationCatalogTest.class.getResourceAsStream("/agent-evals.csv")),
                StandardCharsets.UTF_8))) {
            return reader.lines().skip(1).map(AgentEvaluationCatalogTest::parse).toList();
        }
    }

    private static EvaluationCase parse(String line) {
        String[] fields = line.split("\\|", -1);
        if (fields.length != 6) {
            throw new IllegalArgumentException("Invalid evaluation row: " + line);
        }
        Set<String> tools = fields[3].isBlank()
                ? Set.of()
                : Arrays.stream(fields[3].split(",")).collect(Collectors.toUnmodifiableSet());
        return new EvaluationCase(
                fields[0], fields[1], EvaluationCase.Route.valueOf(fields[2]), tools,
                EvaluationCase.Outcome.valueOf(fields[4]), fields[5]);
    }
}
