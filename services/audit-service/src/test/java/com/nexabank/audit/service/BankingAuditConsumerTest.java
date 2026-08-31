package com.nexabank.audit.service;

import com.nexabank.audit.event.TransferCompletedEvent;
import com.nexabank.audit.repository.AuditRecordRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BankingAuditConsumerTest {
    @Test
    void duplicateEventCreatesOnlyOneAuditRecord() {
        AuditRecordRepository repository = mock(AuditRecordRepository.class);
        when(repository.existsById("EVT-1")).thenReturn(false, true);
        BankingAuditConsumer consumer = new BankingAuditConsumer(repository, new ObjectMapper());
        TransferCompletedEvent event = new TransferCompletedEvent("EVT-1", "TRF-1", "D-1", "C-1",
                "ACC-1", "CUS-1", "ACC-2", "CUS-2", new BigDecimal("50.00"), "INR",
                Instant.parse("2026-08-31T10:00:00Z"), "REQ-1");

        consumer.recordTransfer(event);
        consumer.recordTransfer(event);

        verify(repository, times(1)).save(any());
    }
}
