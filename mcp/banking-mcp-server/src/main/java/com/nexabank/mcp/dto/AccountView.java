package com.nexabank.mcp.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountView(String accountId, String customerId, String accountNumber, String type,
        BigDecimal balance, String currency, String status, Instant createdAt) {
}
