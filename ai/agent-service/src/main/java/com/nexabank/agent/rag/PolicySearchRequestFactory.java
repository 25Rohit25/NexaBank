package com.nexabank.agent.rag;

import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PolicySearchRequestFactory {
    private final int topK;
    private final double similarityThreshold;

    public PolicySearchRequestFactory(
            @Value("${nexa.rag.top-k:4}") int topK,
            @Value("${nexa.rag.similarity-threshold:0.72}") double similarityThreshold) {
        if (topK < 1 || topK > 20) throw new IllegalArgumentException("RAG top-k must be between 1 and 20");
        if (similarityThreshold < 0 || similarityThreshold > 1) {
            throw new IllegalArgumentException("RAG similarity threshold must be between 0 and 1");
        }
        this.topK = topK;
        this.similarityThreshold = similarityThreshold;
    }

    public SearchRequest create() {
        return SearchRequest.builder()
                .query("placeholder replaced by QuestionAnswerAdvisor")
                .topK(topK)
                .similarityThreshold(similarityThreshold)
                .filterExpression("documentType == 'bank_policy'")
                .build();
    }
}
