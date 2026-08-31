package com.nexabank.mcp.tool;

import com.nexabank.mcp.client.BankingApiClient;
import com.nexabank.mcp.dto.AccountView;
import com.nexabank.mcp.dto.BalanceView;
import com.nexabank.mcp.security.AuthenticatedUser;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BankingReadToolsTest {
    @Test
    void accountListingAlwaysUsesAuthenticatedSubjectAndRelaysToken() {
        BankingApiClient api = mock(BankingApiClient.class);
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        when(user.customerId()).thenReturn("CUS-1001");
        when(user.bearerToken()).thenReturn("verified-jwt");
        when(api.getCustomerAccounts("CUS-1001", "verified-jwt")).thenReturn(List.of());

        new BankingReadTools(api, user).getCustomerAccounts();

        verify(api).getCustomerAccounts("CUS-1001", "verified-jwt");
    }

    @Test
    void balanceToolRelaysVerifiedTokenToDeterministicApi() {
        BankingApiClient api = mock(BankingApiClient.class);
        AuthenticatedUser user = mock(AuthenticatedUser.class);
        when(user.bearerToken()).thenReturn("verified-jwt");
        when(api.getAccountBalance("ACC-1", "verified-jwt"))
                .thenReturn(new BalanceView("ACC-1", new BigDecimal("42.00"), "INR"));

        BalanceView result = new BankingReadTools(api, user).getAccountBalance("ACC-1");

        assertThat(result.balance()).isEqualByComparingTo("42.00");
        verify(api).getAccountBalance("ACC-1", "verified-jwt");
    }
}
