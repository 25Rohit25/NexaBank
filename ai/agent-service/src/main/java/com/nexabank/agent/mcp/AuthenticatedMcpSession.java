package com.nexabank.agent.mcp;

import io.modelcontextprotocol.client.McpSyncClient;
import org.springframework.ai.tool.ToolCallbackProvider;

public class AuthenticatedMcpSession implements AutoCloseable {
    private final McpSyncClient client;
    private final ToolCallbackProvider tools;

    AuthenticatedMcpSession(McpSyncClient client, ToolCallbackProvider tools) {
        this.client = client;
        this.tools = tools;
    }

    public ToolCallbackProvider tools() {
        return tools;
    }

    @Override
    public void close() {
        client.close();
    }
}
