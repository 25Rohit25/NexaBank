package com.nexabank.transaction.service;

import com.nexabank.transaction.event.TransferCompletedEvent;
import com.nexabank.transaction.repository.BankTransactionRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionProjectionConsumerTest {
    @Test
    void duplicateTransferEventDoesNotDuplicateEitherTransaction() {
        BankTransactionRepository repository = mock(BankTransactionRepository.class);
        when(repository.existsById("DEBIT-1")).thenReturn(false, true);
        when(repository.existsById("CREDIT-1")).thenReturn(false, true);
        TransactionProjectionConsumer consumer = new TransactionProjectionConsumer(repository, new ObjectMapper());
        TransferCompletedEvent event = new TransferCompletedEvent("EVT-1", "TRF-1", "DEBIT-1", "CREDIT-1",
                "ACC-1", "CUS-1", "ACC-2", "CUS-2", new BigDecimal("25.00"), "INR",
                Instant.parse("2026-08-31T10:00:00Z"), "REQ-1");

        consumer.projectTransfer(event);
        consumer.projectTransfer(event);

        verify(repository, times(2)).save(any());
    }
}
