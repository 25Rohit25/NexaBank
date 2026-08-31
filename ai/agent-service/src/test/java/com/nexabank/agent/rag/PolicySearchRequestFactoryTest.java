package com.nexabank.agent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.vectorstore.SearchRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolicySearchRequestFactoryTest {
    @Test
    void restrictsRetrievalToRelevantBankPolicies() {
        SearchRequest request = new PolicySearchRequestFactory(4, 0.72).create();

        assertThat(request.getTopK()).isEqualTo(4);
        assertThat(request.getSimilarityThreshold()).isEqualTo(0.72);
        assertThat(request.getFilterExpression().toString()).contains("documentType", "bank_policy");
    }

    @Test
    void rejectsUnsafeRetrievalConfiguration() {
        assertThatThrownBy(() -> new PolicySearchRequestFactory(0, 0.72))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new PolicySearchRequestFactory(4, 1.01))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
