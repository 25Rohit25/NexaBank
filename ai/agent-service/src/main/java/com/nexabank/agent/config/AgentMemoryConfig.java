package com.nexabank.agent.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AgentMemoryConfig {
    @Bean
    ChatMemory agentChatMemory(
            ChatMemoryRepository repository,
            @Value("${nexa.memory.max-messages:20}") int maxMessages) {
        if (maxMessages < 2 || maxMessages > 100) {
            throw new IllegalArgumentException("Agent memory max-messages must be between 2 and 100");
        }
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(maxMessages)
                .build();
    }
}
