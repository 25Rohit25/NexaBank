package com.nexabank.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @Column(length = 32, nullable = false, updatable = false)
    private String id;

    @Column(name = "customer_id", length = 32, nullable = false, updatable = false)
    private String customerId;

    @Column(name = "account_number", length = 16, nullable = false, unique = true, updatable = false)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", length = 20, nullable = false)
    private AccountType accountType;

    @Column(precision = 19, scale = 2, nullable = false)
    private BigDecimal balance;

    @Column(length = 3, nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private AccountStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Version
    private long version;

    protected Account() {
    }

    public Account(String id, String customerId, String accountNumber, AccountType accountType, String currency) {
        this.id = id;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.accountType = accountType;
        this.balance = BigDecimal.ZERO.setScale(2);
        this.currency = currency;
        this.status = AccountStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getCustomerId() { return customerId; }
    public String getAccountNumber() { return accountNumber; }
    public AccountType getAccountType() { return accountType; }
    public BigDecimal getBalance() { return balance; }
    public String getCurrency() { return currency; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}

