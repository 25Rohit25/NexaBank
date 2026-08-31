package com.nexabank.transaction.service;

import com.nexabank.transaction.entity.BankTransaction;
import com.nexabank.transaction.entity.TransactionStatus;
import com.nexabank.transaction.entity.TransactionType;
import com.nexabank.transaction.repository.BankTransactionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionQueryServiceTest {
    @Test
    void customerCannotReadAnotherCustomersTransaction() {
        BankTransactionRepository repository = mock(BankTransactionRepository.class);
        BankTransaction transaction = new BankTransaction("TX-1", null, "ACC-2", "CUS-2", null,
                TransactionType.DEPOSIT, new BigDecimal("10.00"), "INR", TransactionStatus.COMPLETED,
                Instant.parse("2026-08-31T10:00:00Z"), "REQ-1");
        when(repository.findById("TX-1")).thenReturn(Optional.of(transaction));

        assertThatThrownBy(() -> new TransactionQueryService(repository).get("TX-1", "CUS-1", false))
                .isInstanceOf(AccessDeniedException.class);
    }
}
