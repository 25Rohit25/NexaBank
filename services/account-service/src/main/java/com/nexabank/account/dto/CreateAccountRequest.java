package com.nexabank.account.dto;

import com.nexabank.account.entity.AccountType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateAccountRequest(
        @NotNull AccountType accountType,
        @NotNull @Pattern(regexp = "^[A-Z]{3}$") String currency
) {
}

