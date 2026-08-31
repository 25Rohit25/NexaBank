package com.nexabank.audit.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionCompletedEvent(String eventId, String transactionId, String accountId,
        String customerId, BigDecimal amount, String currency, Instant occurredAt, String correlationId) {
}
