package com.nexabank.transaction.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "transactions")
public class BankTransaction {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "transfer_id", length = 36, updatable = false)
    private String transferId;

    @Column(name = "account_id", length = 32, nullable = false, updatable = false)
    private String accountId;

    @Column(name = "customer_id", length = 32, nullable = false, updatable = false)
    private String customerId;

    @Column(name = "counterparty_account_id", length = 32, updatable = false)
    private String counterpartyAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", length = 24, nullable = false, updatable = false)
    private TransactionType transactionType;

    @Column(precision = 19, scale = 2, nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(length = 3, nullable = false, updatable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private TransactionStatus status;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "correlation_id", length = 64, nullable = false, updatable = false)
    private String correlationId;

    protected BankTransaction() {}

    public BankTransaction(String id, String transferId, String accountId, String customerId,
                           String counterpartyAccountId, TransactionType transactionType, BigDecimal amount,
                           String currency, TransactionStatus status, Instant occurredAt, String correlationId) {
        this.id = id;
        this.transferId = transferId;
        this.accountId = accountId;
        this.customerId = customerId;
        this.counterpartyAccountId = counterpartyAccountId;
        this.transactionType = transactionType;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
    }

    public String getId() { return id; }
    public String getTransferId() { return transferId; }
    public String getAccountId() { return accountId; }
    public String getCustomerId() { return customerId; }
    public String getCounterpartyAccountId() { return counterpartyAccountId; }
    public TransactionType getTransactionType() { return transactionType; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public TransactionStatus getStatus() { return status; }
    public Instant getOccurredAt() { return occurredAt; }
    public String getCorrelationId() { return correlationId; }
}
