package com.nexabank.mcp.tool;

import com.nexabank.mcp.client.BankingApiClient;
import com.nexabank.mcp.dto.AccountView;
import com.nexabank.mcp.dto.TransferResult;
import com.nexabank.mcp.security.AuthenticatedUser;
import com.nexabank.mcp.service.TransferConfirmationStore;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BankingTransferToolsTest {
    @Test
    void prepareValidatesOwnedAccountsAndReturnsProjectedBalanceWithoutExecuting() {
        BankingApiClient api = mock(BankingApiClient.class);
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        TransferConfirmationStore store = mock(TransferConfirmationStore.class);
        when(user.customerId()).thenReturn("CUS-1");
        when(user.bearerToken()).thenReturn("jwt");
        when(api.getCustomerAccounts("CUS-1", "jwt")).thenReturn(List.of(
                account("ACC-S", "SAVINGS", "1000.00"), account("ACC-C", "CURRENT", "200.00")));
        Instant expiry = Instant.parse("2026-08-31T12:05:00Z");
        when(store.create("CUS-1", "ACC-S", "ACC-C", new BigDecimal("250.00")))
                .thenReturn(new TransferConfirmationStore.PendingTransfer("confirm-1", "CUS-1", "ACC-S",
                        "ACC-C", new BigDecimal("250.00"), expiry));

        var preview = new BankingTransferTools(api, user, store)
                .prepareTransfer("ACC-S", "ACC-C", new BigDecimal("250"));

        assertThat(preview.confirmationToken()).isEqualTo("confirm-1");
        assertThat(preview.projectedBalance()).isEqualByComparingTo("750.00");
    }

    @Test
    void executeUsesOnlyStoredDetailsAndCompletesTokenAfterBackendSuccess() {
        BankingApiClient api = mock(BankingApiClient.class);
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        TransferConfirmationStore store = mock(TransferConfirmationStore.class);
        when(user.customerId()).thenReturn("CUS-1");
        when(user.bearerToken()).thenReturn("jwt");
        var pending = new TransferConfirmationStore.PendingTransfer("confirm-1", "CUS-1", "ACC-S",
                "ACC-C", new BigDecimal("250.00"), Instant.now().plusSeconds(60));
        when(store.require("confirm-1", "CUS-1")).thenReturn(pending);
        TransferResult result = new TransferResult("TRF-1", "D-1", "C-1", "ACC-S", "ACC-C",
                new BigDecimal("250.00"), "INR", "COMPLETED", Instant.now());
        when(api.executeTransfer("ACC-S", "ACC-C", new BigDecimal("250.00"), "confirm-1", "jwt"))
                .thenReturn(result);

        var actual = new BankingTransferTools(api, user, store).executeTransfer("confirm-1");

        assertThat(actual.transferId()).isEqualTo("TRF-1");
        verify(store).complete("confirm-1");
    }

    private AccountView account(String id, String type, String balance) {
        return new AccountView(id, "CUS-1", "911111111111", type, new BigDecimal(balance),
                "INR", "ACTIVE", Instant.now());
    }
}
