package com.nexabank.agent.service;

import com.nexabank.agent.mcp.AuthenticatedMcpClientFactory;
import com.nexabank.agent.mcp.AuthenticatedMcpSession;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AgentChatService {
    private final ChatClient chatClient;
    private final AgentPromptFactory promptFactory;
    private final AuthenticatedMcpClientFactory mcpClientFactory;

    public AgentChatService(ChatClient.Builder chatClientBuilder, AgentPromptFactory promptFactory,
                            AuthenticatedMcpClientFactory mcpClientFactory) {
        this.chatClient = chatClientBuilder.build();
        this.promptFactory = promptFactory;
        this.mcpClientFactory = mcpClientFactory;
    }

    public String chat(String customerId, String bearerToken, String message) {
        try (AuthenticatedMcpSession mcp = mcpClientFactory.open(bearerToken)) {
            return chatClient.prompt()
                    .system(promptFactory.systemPrompt(customerId))
                    .user(message)
                    .tools(mcp.tools())
                    .call()
                    .content();
        }
    }
}
