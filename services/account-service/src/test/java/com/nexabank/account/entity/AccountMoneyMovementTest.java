package com.nexabank.account.entity;

import com.nexabank.account.exception.BusinessRuleException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountMoneyMovementTest {

    @Test
    void depositDebitAndCreditPreserveExactDecimalBalance() {
        Account account = account();

        account.deposit(new BigDecimal("125.50"));
        account.debit(new BigDecimal("25.25"));
        account.credit(new BigDecimal("4.75"));

        assertThat(account.getBalance()).isEqualByComparingTo("105.00");
    }

    @Test
    void debitRejectsInsufficientFundsWithoutChangingBalance() {
        Account account = account();
        account.deposit(new BigDecimal("10.00"));

        assertThatThrownBy(() -> account.debit(new BigDecimal("10.01")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Insufficient funds");
        assertThat(account.getBalance()).isEqualByComparingTo("10.00");
    }

    private Account account() {
        return new Account("ACC-TEST", "CUS-TEST", "911111111111", AccountType.SAVINGS, "INR");
    }
}
