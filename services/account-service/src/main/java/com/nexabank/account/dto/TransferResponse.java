package com.nexabank.account.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferResponse(String transferId, String debitTransactionId, String creditTransactionId,
                               String sourceAccountId, String destinationAccountId, BigDecimal amount,
                               String currency, String status, Instant completedAt) {
}
