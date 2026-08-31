package com.nexabank.mcp.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionView(String transactionId, String transferId, String accountId,
        String counterpartyAccountId, String type, BigDecimal amount, String currency,
        String status, Instant occurredAt, String correlationId) {
}
