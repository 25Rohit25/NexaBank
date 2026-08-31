package com.nexabank.agent.rag;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class PolicyIngestionService {
    private static final Pattern POLICY_ID = Pattern.compile("[A-Z0-9-]+");

    private final PolicyDocumentLoader loader;
    private final VectorStore vectorStore;

    public PolicyIngestionService(PolicyDocumentLoader loader, VectorStore vectorStore) {
        this.loader = loader;
        this.vectorStore = vectorStore;
    }

    public int refresh() {
        List<Document> chunks = loader.load();
        chunks.stream()
                .map(document -> document.getMetadata().get("policyId"))
                .map(Object::toString)
                .distinct()
                .sorted()
                .forEach(this::deleteExistingPolicy);
        vectorStore.add(chunks);
        return chunks.size();
    }

    private void deleteExistingPolicy(String policyId) {
        if (!POLICY_ID.matcher(policyId).matches()) {
            throw new IllegalStateException("Invalid policy ID metadata: " + policyId);
        }
        vectorStore.delete("policyId == '" + policyId + "'");
    }
}
