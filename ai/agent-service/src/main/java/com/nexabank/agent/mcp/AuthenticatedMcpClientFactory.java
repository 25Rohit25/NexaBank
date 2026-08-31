package com.nexabank.agent.mcp;

import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import org.springframework.ai.mcp.McpToolNamePrefixGenerator;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class AuthenticatedMcpClientFactory {
    private final String mcpUrl;

    public AuthenticatedMcpClientFactory(@Value("${banking.mcp-url}") String mcpUrl) {
        this.mcpUrl = mcpUrl;
    }

    public AuthenticatedMcpSession open(String bearerToken) {
        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport.builder(mcpUrl)
                .endpoint("/mcp")
                .httpRequestCustomizer(new BearerTokenRequestCustomizer(bearerToken))
                .connectTimeout(Duration.ofSeconds(5))
                .build();
        McpSyncClient client = McpClient.sync(transport)
                .requestTimeout(Duration.ofSeconds(20))
                .build();
        try {
            client.initialize();
            SyncMcpToolCallbackProvider tools = SyncMcpToolCallbackProvider.builder()
                    .addMcpClient(client)
                    .toolNamePrefixGenerator(McpToolNamePrefixGenerator.noPrefix())
                    .build();
            return new AuthenticatedMcpSession(client, tools);
        } catch (RuntimeException exception) {
            client.close();
            throw exception;
        }
    }
}
