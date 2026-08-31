package com.nexabank.agent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyIngestionServiceTest {
    @Test
    void replacesExistingPolicyChunksBeforeAddingTheCurrentCorpus() {
        PolicyDocumentLoader loader = mock(PolicyDocumentLoader.class);
        VectorStore vectorStore = mock(VectorStore.class);
        List<Document> chunks = List.of(
                document("fees one", "NEXA-POL-FEE-001"),
                document("fees two", "NEXA-POL-FEE-001"),
                document("limits", "NEXA-POL-LIMIT-001"));
        when(loader.load()).thenReturn(chunks);

        int count = new PolicyIngestionService(loader, vectorStore).refresh();

        assertThat(count).isEqualTo(3);
        var ordered = inOrder(vectorStore);
        ordered.verify(vectorStore).delete("policyId == 'NEXA-POL-FEE-001'");
        ordered.verify(vectorStore).delete("policyId == 'NEXA-POL-LIMIT-001'");
        ordered.verify(vectorStore).add(chunks);
    }

    @Test
    void rejectsUnsafePolicyMetadataBeforeBuildingAFilter() {
        PolicyDocumentLoader loader = mock(PolicyDocumentLoader.class);
        VectorStore vectorStore = mock(VectorStore.class);
        when(loader.load()).thenReturn(List.of(document("unsafe", "x' || true")));

        assertThatThrownBy(() -> new PolicyIngestionService(loader, vectorStore).refresh())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid policy ID");
    }

    private Document document(String text, String policyId) {
        return new Document(text, Map.of("policyId", policyId, "documentType", "bank_policy"));
    }
}
