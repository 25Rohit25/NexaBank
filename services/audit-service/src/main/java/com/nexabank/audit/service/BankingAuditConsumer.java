package com.nexabank.audit.service;

import com.nexabank.audit.entity.AuditRecord;
import com.nexabank.audit.event.TransactionCompletedEvent;
import com.nexabank.audit.event.TransferCompletedEvent;
import com.nexabank.audit.repository.AuditRecordRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class BankingAuditConsumer {
    private final AuditRecordRepository auditRecords;
    private final ObjectMapper objectMapper;

    public BankingAuditConsumer(AuditRecordRepository auditRecords, ObjectMapper objectMapper) {
        this.auditRecords = auditRecords;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "bank.transfer.completed")
    public void consumeTransfer(String json) throws Exception {
        recordTransfer(objectMapper.readValue(json, TransferCompletedEvent.class));
    }

    @KafkaListener(topics = "bank.transaction.completed")
    public void consumeTransaction(String json) throws Exception {
        recordDeposit(objectMapper.readValue(json, TransactionCompletedEvent.class));
    }

    @Transactional
    public void recordTransfer(TransferCompletedEvent event) {
        if (!auditRecords.existsById(event.eventId())) {
            auditRecords.save(new AuditRecord(event.eventId(), event.sourceCustomerId(), "TRANSFER_COMPLETED",
                    "TRANSFER", event.transferId(), event.occurredAt(), "COMPLETED", event.correlationId()));
        }
    }

    @Transactional
    public void recordDeposit(TransactionCompletedEvent event) {
        if (!auditRecords.existsById(event.eventId())) {
            auditRecords.save(new AuditRecord(event.eventId(), event.customerId(), "DEPOSIT_COMPLETED",
                    "TRANSACTION", event.transactionId(), event.occurredAt(), "COMPLETED", event.correlationId()));
        }
    }
}
