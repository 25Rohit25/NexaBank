package com.nexabank.account.dto;

import com.nexabank.account.entity.Account;
import com.nexabank.account.entity.AccountStatus;
import com.nexabank.account.entity.AccountType;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountResponse(
        String accountId,
        String customerId,
        String accountNumber,
        AccountType type,
        BigDecimal balance,
        String currency,
        AccountStatus status,
        Instant createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(account.getId(), account.getCustomerId(), account.getAccountNumber(),
                account.getAccountType(), account.getBalance(), account.getCurrency(),
                account.getStatus(), account.getCreatedAt());
    }
}

