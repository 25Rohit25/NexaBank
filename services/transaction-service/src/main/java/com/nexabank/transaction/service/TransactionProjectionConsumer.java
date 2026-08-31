package com.nexabank.transaction.service;

import com.nexabank.transaction.entity.BankTransaction;
import com.nexabank.transaction.entity.TransactionStatus;
import com.nexabank.transaction.entity.TransactionType;
import com.nexabank.transaction.event.TransactionCompletedEvent;
import com.nexabank.transaction.event.TransferCompletedEvent;
import com.nexabank.transaction.repository.BankTransactionRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
public class TransactionProjectionConsumer {
    private final BankTransactionRepository transactions;
    private final ObjectMapper objectMapper;

    public TransactionProjectionConsumer(BankTransactionRepository transactions, ObjectMapper objectMapper) {
        this.transactions = transactions;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "bank.transfer.completed")
    public void consumeTransfer(String json) throws Exception {
        projectTransfer(objectMapper.readValue(json, TransferCompletedEvent.class));
    }

    @KafkaListener(topics = "bank.transaction.completed")
    public void consumeTransaction(String json) throws Exception {
        projectDeposit(objectMapper.readValue(json, TransactionCompletedEvent.class));
    }

    @Transactional
    public void projectTransfer(TransferCompletedEvent event) {
        if (!transactions.existsById(event.debitTransactionId())) {
            transactions.save(new BankTransaction(event.debitTransactionId(), event.transferId(),
                    event.sourceAccountId(), event.sourceCustomerId(), event.destinationAccountId(),
                    TransactionType.TRANSFER_OUT, event.amount(), event.currency(), TransactionStatus.COMPLETED,
                    event.occurredAt(), event.correlationId()));
        }
        if (!transactions.existsById(event.creditTransactionId())) {
            transactions.save(new BankTransaction(event.creditTransactionId(), event.transferId(),
                    event.destinationAccountId(), event.destinationCustomerId(), event.sourceAccountId(),
                    TransactionType.TRANSFER_IN, event.amount(), event.currency(), TransactionStatus.COMPLETED,
                    event.occurredAt(), event.correlationId()));
        }
    }

    @Transactional
    public void projectDeposit(TransactionCompletedEvent event) {
        if (!transactions.existsById(event.transactionId())) {
            transactions.save(new BankTransaction(event.transactionId(), null, event.accountId(),
                    event.customerId(), null, TransactionType.DEPOSIT, event.amount(), event.currency(),
                    TransactionStatus.COMPLETED, event.occurredAt(), event.correlationId()));
        }
    }
}
