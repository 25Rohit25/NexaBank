package com.nexabank.mcp;

import com.nexabank.mcp.client.BankingApiClient;
import com.nexabank.mcp.dto.AccountView;
import com.nexabank.mcp.dto.BalanceView;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankingMcpBalanceIntegrationTest {
    @LocalServerPort
    private int port;

    @MockitoBean
    private BankingApiClient bankingApi;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    void configureAuthenticatedCustomer(JwtDecoder decoder) {
        Jwt jwt = Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .subject("CUS-1001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(decoder.decode("signed-token")).thenReturn(jwt);
    }

    @Test
    void retrievesTheAuthenticatedCustomersAccountAndBalanceOverStreamableHttp() {
        AccountView account = new AccountView("ACC-1001", "CUS-1001", "XXXX1001", "SAVINGS",
                new BigDecimal("72450.00"), "INR", "ACTIVE", Instant.parse("2026-01-01T00:00:00Z"));
        when(bankingApi.getCustomerAccounts("CUS-1001", "signed-token")).thenReturn(List.of(account));
        when(bankingApi.getAccountBalance("ACC-1001", "signed-token"))
                .thenReturn(new BalanceView("ACC-1001", new BigDecimal("72450.00"), "INR"));

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder("http://localhost:" + port)
                .endpoint("/mcp")
                .httpRequestCustomizer((request, method, endpoint, body, context) ->
                        request.header("Authorization", "Bearer signed-token"))
                .build();

        try (McpSyncClient client = McpClient.sync(transport).build()) {
            client.initialize();
            assertThat(client.listTools().tools())
                    .extracting(McpSchema.Tool::name)
                    .contains("getCustomerAccounts", "getAccountBalance");

            McpSchema.CallToolResult accounts = client.callTool(McpSchema.CallToolRequest
                    .builder("getCustomerAccounts")
                    .arguments(Map.of())
                    .build());
            McpSchema.CallToolResult balance = client.callTool(McpSchema.CallToolRequest
                    .builder("getAccountBalance")
                    .arguments(Map.of("accountId", "ACC-1001"))
                    .build());

            assertThat(accounts.isError()).isNotEqualTo(true);
            assertThat(balance.isError()).isNotEqualTo(true);
            assertThat(balance.content().toString()).contains("72450.00", "INR");
        }

        verify(bankingApi).getCustomerAccounts("CUS-1001", "signed-token");
        verify(bankingApi).getAccountBalance("ACC-1001", "signed-token");
    }
}
