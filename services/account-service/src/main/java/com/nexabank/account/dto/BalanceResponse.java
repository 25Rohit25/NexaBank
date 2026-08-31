package com.nexabank.account.dto;

import java.math.BigDecimal;

public record BalanceResponse(String accountId, BigDecimal balance, String currency) {
}

