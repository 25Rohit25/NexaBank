package com.nexabank.mcp.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferPreview(String confirmationToken, String sourceAccountId, String destinationAccountId,
        BigDecimal amount, String currency, BigDecimal currentBalance, BigDecimal projectedBalance,
        Instant expiresAt, String instruction) {
}
