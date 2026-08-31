package com.nexabank.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "transfer_id", length = 36, updatable = false)
    private String transferId;

    @Column(name = "account_id", length = 32, nullable = false, updatable = false)
    private String accountId;

    @Column(name = "customer_id", length = 32, nullable = false, updatable = false)
    private String customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", length = 24, nullable = false, updatable = false)
    private LedgerEntryType entryType;

    @Column(precision = 19, scale = 2, nullable = false, updatable = false)
    private BigDecimal amount;

    @Column(length = 3, nullable = false, updatable = false)
    private String currency;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected LedgerEntry() {}

    public LedgerEntry(String id, String transferId, String accountId, String customerId,
                       LedgerEntryType entryType, BigDecimal amount, String currency, Instant occurredAt) {
        this.id = id;
        this.transferId = transferId;
        this.accountId = accountId;
        this.customerId = customerId;
        this.entryType = entryType;
        this.amount = amount;
        this.currency = currency;
        this.occurredAt = occurredAt;
    }

    public String getId() { return id; }
}
