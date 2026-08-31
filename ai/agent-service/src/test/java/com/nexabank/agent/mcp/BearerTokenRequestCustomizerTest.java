package com.nexabank.agent.mcp;

import io.modelcontextprotocol.common.McpTransportContext;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.http.HttpRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BearerTokenRequestCustomizerTest {
    @Test
    void addsTheCurrentUsersBearerTokenToEveryMcpRequest() {
        URI endpoint = URI.create("http://localhost:8090/mcp");
        HttpRequest.Builder request = HttpRequest.newBuilder(endpoint);

        new BearerTokenRequestCustomizer("signed-token")
                .customize(request, "POST", endpoint, "{}", McpTransportContext.EMPTY);

        assertThat(request.build().headers().firstValue("Authorization"))
                .contains("Bearer signed-token");
    }

    @Test
    void refusesToCreateAnUnauthenticatedMcpClient() {
        assertThatThrownBy(() -> new BearerTokenRequestCustomizer(" "))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
