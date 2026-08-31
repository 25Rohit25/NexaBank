package com.nexabank.account.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record DepositResponse(String transactionId, String accountId, BigDecimal amount,
                              BigDecimal balance, String currency, Instant completedAt) {
}
