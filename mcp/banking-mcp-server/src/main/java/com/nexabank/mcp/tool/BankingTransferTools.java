package com.nexabank.mcp.tool;

import com.nexabank.mcp.client.BankingApiClient;
import com.nexabank.mcp.dto.AccountView;
import com.nexabank.mcp.dto.TransferPreview;
import com.nexabank.mcp.dto.TransferResult;
import com.nexabank.mcp.security.AuthenticatedUser;
import com.nexabank.mcp.service.TransferConfirmationStore;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
public class BankingTransferTools {
    private final BankingApiClient bankingApi;
    private final AuthenticatedUser user;
    private final TransferConfirmationStore confirmations;

    public BankingTransferTools(BankingApiClient bankingApi, AuthenticatedUser user,
                                TransferConfirmationStore confirmations) {
        this.bankingApi = bankingApi;
        this.user = user;
        this.confirmations = confirmations;
    }

    @McpTool(name = "prepareTransfer", description = "Validate a transfer between the authenticated customer's accounts and return a short-lived confirmation token. This does not move money.")
    public TransferPreview prepareTransfer(
            @McpToolParam(description = "Source account identifier", required = true) String sourceAccountId,
            @McpToolParam(description = "Destination account identifier", required = true) String destinationAccountId,
            @McpToolParam(description = "Positive transfer amount", required = true) BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (sourceAccountId.equals(destinationAccountId)) {
            throw new IllegalArgumentException("Source and destination accounts must differ");
        }
        String customerId = user.customerId();
        String bearerToken = user.bearerToken();
        List<AccountView> accounts = bankingApi.getCustomerAccounts(customerId, bearerToken);
        AccountView source = find(accounts, sourceAccountId);
        AccountView destination = find(accounts, destinationAccountId);
        if (!source.currency().equals(destination.currency())) {
            throw new IllegalArgumentException("Cross-currency transfers are not supported");
        }
        BigDecimal normalizedAmount = amount.setScale(2);
        if (source.balance().compareTo(normalizedAmount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }
        var pending = confirmations.create(customerId, sourceAccountId, destinationAccountId, normalizedAmount);
        return new TransferPreview(pending.token(), sourceAccountId, destinationAccountId, normalizedAmount,
                source.currency(), source.balance(), source.balance().subtract(normalizedAmount), pending.expiresAt(),
                "Ask the user to confirm these exact details, then call executeTransfer with confirmationToken.");
    }

    @McpTool(name = "executeTransfer", description = "Execute only a previously prepared transfer after explicit user confirmation. Requires the unexpired confirmation token; transfer details cannot be changed here.")
    public TransferResult executeTransfer(
            @McpToolParam(description = "Token returned by prepareTransfer after the user explicitly confirms", required = true)
            String confirmationToken) {
        String customerId = user.customerId();
        var pending = confirmations.require(confirmationToken, customerId);
        TransferResult result = bankingApi.executeTransfer(pending.sourceAccountId(), pending.destinationAccountId(),
                pending.amount(), confirmationToken, user.bearerToken());
        confirmations.complete(confirmationToken);
        return result;
    }

    private AccountView find(List<AccountView> accounts, String accountId) {
        return accounts.stream().filter(account -> account.accountId().equals(accountId)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Account is not owned by the authenticated customer: " + accountId));
    }
}
