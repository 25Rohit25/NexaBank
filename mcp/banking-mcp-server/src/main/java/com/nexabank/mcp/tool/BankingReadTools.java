package com.nexabank.mcp.tool;

import com.nexabank.mcp.client.BankingApiClient;
import com.nexabank.mcp.dto.AccountView;
import com.nexabank.mcp.dto.BalanceView;
import com.nexabank.mcp.dto.TransactionView;
import com.nexabank.mcp.security.AuthenticatedUser;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BankingReadTools {
    private final BankingApiClient bankingApi;
    private final AuthenticatedUser user;

    public BankingReadTools(BankingApiClient bankingApi, AuthenticatedUser user) {
        this.bankingApi = bankingApi;
        this.user = user;
    }

    @McpTool(name = "getCustomerAccounts", description = "List bank accounts owned by the authenticated customer")
    public List<AccountView> getCustomerAccounts() {
        return bankingApi.getCustomerAccounts(user.customerId(), user.bearerToken());
    }

    @McpTool(name = "getAccountBalance", description = "Get the current balance of an account accessible to the authenticated customer")
    public BalanceView getAccountBalance(
            @McpToolParam(description = "Nexa Bank account identifier", required = true) String accountId) {
        return bankingApi.getAccountBalance(accountId, user.bearerToken());
    }

    @McpTool(name = "getTransactions", description = "Get accessible account transactions with optional ISO-8601 date and minimum amount filters")
    public List<TransactionView> getTransactions(
            @McpToolParam(description = "Nexa Bank account identifier", required = true) String accountId,
            @McpToolParam(description = "Optional inclusive ISO-8601 start instant", required = false) String from,
            @McpToolParam(description = "Optional inclusive ISO-8601 end instant", required = false) String to,
            @McpToolParam(description = "Optional minimum transaction amount", required = false) BigDecimal minAmount) {
        return bankingApi.getTransactions(accountId, from, to, minAmount, user.bearerToken());
    }
}
