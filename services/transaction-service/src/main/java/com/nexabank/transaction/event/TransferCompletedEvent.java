package com.nexabank.transaction.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TransferCompletedEvent(String eventId, String transferId, String debitTransactionId,
        String creditTransactionId, String sourceAccountId, String sourceCustomerId,
        String destinationAccountId, String destinationCustomerId, BigDecimal amount,
        String currency, Instant occurredAt, String correlationId) {
}
