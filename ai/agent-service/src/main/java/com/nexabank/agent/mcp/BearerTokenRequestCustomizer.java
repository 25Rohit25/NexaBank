package com.nexabank.agent.mcp;

import io.modelcontextprotocol.client.transport.customizer.McpSyncHttpClientRequestCustomizer;
import io.modelcontextprotocol.common.McpTransportContext;

import java.net.URI;
import java.net.http.HttpRequest;

public class BearerTokenRequestCustomizer implements McpSyncHttpClientRequestCustomizer {
    private final String bearerToken;

    public BearerTokenRequestCustomizer(String bearerToken) {
        if (bearerToken == null || bearerToken.isBlank()) {
            throw new IllegalArgumentException("bearerToken is required");
        }
        this.bearerToken = bearerToken;
    }

    @Override
    public void customize(HttpRequest.Builder requestBuilder, String method, URI endpoint,
                          String body, McpTransportContext context) {
        requestBuilder.header("Authorization", "Bearer " + bearerToken);
    }
}
