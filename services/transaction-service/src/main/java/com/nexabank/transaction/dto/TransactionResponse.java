package com.nexabank.transaction.dto;

import com.nexabank.transaction.entity.BankTransaction;
import com.nexabank.transaction.entity.TransactionStatus;
import com.nexabank.transaction.entity.TransactionType;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(String transactionId, String transferId, String accountId,
        String counterpartyAccountId, TransactionType type, BigDecimal amount, String currency,
        TransactionStatus status, Instant occurredAt, String correlationId) {
    public static TransactionResponse from(BankTransaction transaction) {
        return new TransactionResponse(transaction.getId(), transaction.getTransferId(), transaction.getAccountId(),
                transaction.getCounterpartyAccountId(), transaction.getTransactionType(), transaction.getAmount(),
                transaction.getCurrency(), transaction.getStatus(), transaction.getOccurredAt(),
                transaction.getCorrelationId());
    }
}
