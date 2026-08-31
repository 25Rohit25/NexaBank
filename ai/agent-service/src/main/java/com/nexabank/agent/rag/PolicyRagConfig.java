package com.nexabank.agent.rag;

import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyRagConfig {
    @Bean
    QuestionAnswerAdvisor policyQuestionAnswerAdvisor(
            VectorStore vectorStore, PolicySearchRequestFactory searchRequestFactory) {
        return QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(searchRequestFactory.create())
                .build();
    }
}
