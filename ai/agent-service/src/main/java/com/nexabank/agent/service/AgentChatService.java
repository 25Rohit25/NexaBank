package com.nexabank.agent.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentChatService {
    private final ChatClient chatClient;
    private final AgentPromptFactory promptFactory;

    public AgentChatService(ChatClient.Builder chatClientBuilder, AgentPromptFactory promptFactory) {
        this.chatClient = chatClientBuilder.build();
        this.promptFactory = promptFactory;
    }

    public String chat(String customerId, String message) {
        return chatClient.prompt()
                .system(promptFactory.systemPrompt(customerId))
                .user(message)
                .call()
                .content();
    }
}
