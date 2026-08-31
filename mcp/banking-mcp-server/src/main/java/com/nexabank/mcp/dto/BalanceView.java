package com.nexabank.mcp.dto;

import java.math.BigDecimal;

public record BalanceView(String accountId, BigDecimal balance, String currency) {
}
