package com.nexabank.agent.rag;

import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class PolicyDocumentLoaderTest {
    @Test
    void loadsAllVersionedPoliciesAndPreservesMetadataAcrossChunks() {
        List<Document> chunks = new PolicyDocumentLoader().load();

        Set<String> sources = chunks.stream()
                .map(document -> document.getMetadata().get("source").toString())
                .collect(Collectors.toSet());
        assertThat(sources).containsExactlyInAnyOrder(
                "account-fees.md",
                "faq.md",
                "international-transfer-policy.md",
                "refund-policy.md",
                "savings-account-policy.md",
                "transfer-limits.md");
        assertThat(chunks).allSatisfy(document -> {
            assertThat(document.getText()).isNotBlank();
            assertThat(document.getMetadata())
                    .containsEntry("documentType", "bank_policy")
                    .containsKeys("policyId", "effectiveDate", "source");
        });
    }
}
