package com.nexabank.mcp;

import com.nexabank.mcp.client.BankingApiClient;
import com.nexabank.mcp.dto.AccountView;
import com.nexabank.mcp.dto.BalanceView;
import com.nexabank.mcp.dto.TransactionView;
import com.nexabank.mcp.dto.TransferResult;
import com.nexabank.mcp.service.TransferConfirmationStore;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockReset;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BankingMcpBalanceIntegrationTest {
    @LocalServerPort
    private int port;

    @MockitoBean
    private BankingApiClient bankingApi;

    @MockitoBean(reset = MockReset.NONE)
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private TransferConfirmationStore confirmations;

    @BeforeEach
    void configureAuthenticatedCustomer() {
        Jwt jwt = Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .subject("CUS-1001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(300))
                .build();
        when(jwtDecoder.decode("signed-token")).thenReturn(jwt);
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

    @Test
    void retrievesTransactionsAboveTheRequestedAmountAndWithinTheDateRange() {
        String from = "2026-09-01T00:00:00Z";
        String to = "2026-09-30T23:59:59Z";
        BigDecimal minimum = new BigDecimal("5000.00");
        TransactionView transaction = new TransactionView(
                "TXN-9001", "TRF-7001", "ACC-1001", "ACC-2001", "TRANSFER_OUT",
                new BigDecimal("6250.00"), "INR", "COMPLETED",
                Instant.parse("2026-09-10T10:15:30Z"), "REQ-9001");
        when(bankingApi.getTransactions(eq("ACC-1001"), eq(from), eq(to),
                any(BigDecimal.class), eq("signed-token")))
                .thenReturn(List.of(transaction));

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
                    .contains("getTransactions");

            McpSchema.CallToolResult result = client.callTool(McpSchema.CallToolRequest
                    .builder("getTransactions")
                    .arguments(Map.of(
                            "accountId", "ACC-1001",
                            "from", from,
                            "to", to,
                            "minAmount", minimum))
                    .build());

            assertThat(result.isError()).isNotEqualTo(true);
            assertThat(result.content().toString())
                    .contains("TXN-9001", "6250.00", "INR", "TRANSFER_OUT");
        }

        ArgumentCaptor<BigDecimal> minimumCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        verify(bankingApi).getTransactions(eq("ACC-1001"), eq(from), eq(to),
                minimumCaptor.capture(), eq("signed-token"));
        assertThat(minimumCaptor.getValue()).isEqualByComparingTo(minimum);
    }

    @Test
    void preparesWithoutMovingMoneyAndExecutesOnlyWithTheConfirmationToken() {
        AccountView savings = new AccountView("ACC-S", "CUS-1001", "XXXX1001", "SAVINGS",
                new BigDecimal("1000.00"), "INR", "ACTIVE", Instant.parse("2026-01-01T00:00:00Z"));
        AccountView current = new AccountView("ACC-C", "CUS-1001", "XXXX2001", "CURRENT",
                new BigDecimal("200.00"), "INR", "ACTIVE", Instant.parse("2026-01-02T00:00:00Z"));
        BigDecimal amount = new BigDecimal("250.00");
        Instant expiry = Instant.parse("2026-09-01T00:30:00Z");
        var pending = new TransferConfirmationStore.PendingTransfer(
                "confirm-1", "CUS-1001", "ACC-S", "ACC-C", amount, expiry);
        when(bankingApi.getCustomerAccounts("CUS-1001", "signed-token"))
                .thenReturn(List.of(savings, current));
        when(confirmations.create("CUS-1001", "ACC-S", "ACC-C", amount)).thenReturn(pending);

        HttpClientStreamableHttpTransport transport = HttpClientStreamableHttpTransport
                .builder("http://localhost:" + port)
                .endpoint("/mcp")
                .httpRequestCustomizer((request, method, endpoint, body, context) ->
                        request.header("Authorization", "Bearer signed-token"))
                .build();

        try (McpSyncClient client = McpClient.sync(transport).build()) {
            client.initialize();
            McpSchema.CallToolResult preview = client.callTool(McpSchema.CallToolRequest
                    .builder("prepareTransfer")
                    .arguments(Map.of(
                            "sourceAccountId", "ACC-S",
                            "destinationAccountId", "ACC-C",
                            "amount", amount))
                    .build());

            assertThat(preview.isError()).isNotEqualTo(true);
            assertThat(preview.content().toString())
                    .contains("confirm-1", "250.00", "750.00", "Ask the user to confirm");
            verify(bankingApi, never()).executeTransfer(
                    anyString(), anyString(), any(BigDecimal.class), anyString(), anyString());

            when(confirmations.require("confirm-1", "CUS-1001")).thenReturn(pending);
            TransferResult completed = new TransferResult(
                    "TRF-1", "TXN-D", "TXN-C", "ACC-S", "ACC-C", amount,
                    "INR", "COMPLETED", Instant.parse("2026-09-01T00:25:00Z"));
            when(bankingApi.executeTransfer("ACC-S", "ACC-C", amount,
                    "confirm-1", "signed-token")).thenReturn(completed);

            McpSchema.CallToolResult result = client.callTool(McpSchema.CallToolRequest
                    .builder("executeTransfer")
                    .arguments(Map.of("confirmationToken", "confirm-1"))
                    .build());

            assertThat(result.isError()).isNotEqualTo(true);
            assertThat(result.content().toString()).contains("TRF-1", "COMPLETED", "250.00");
        }

        verify(bankingApi).executeTransfer(
                "ACC-S", "ACC-C", amount, "confirm-1", "signed-token");
        verify(confirmations).complete("confirm-1");
    }
}
