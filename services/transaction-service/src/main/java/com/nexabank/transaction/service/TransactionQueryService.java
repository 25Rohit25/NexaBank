package com.nexabank.transaction.service;

import com.nexabank.transaction.dto.TransactionResponse;
import com.nexabank.transaction.entity.BankTransaction;
import com.nexabank.transaction.exception.ResourceNotFoundException;
import com.nexabank.transaction.repository.BankTransactionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Service
public class TransactionQueryService {
    private final BankTransactionRepository transactions;

    public TransactionQueryService(BankTransactionRepository transactions) {
        this.transactions = transactions;
    }

    @Transactional(readOnly = true)
    public TransactionResponse get(String transactionId, String actorId, boolean admin) {
        BankTransaction transaction = transactions.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + transactionId));
        requireOwner(transaction, actorId, admin);
        return TransactionResponse.from(transaction);
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> accountHistory(String accountId, String actorId, boolean admin,
            Instant from, Instant to, BigDecimal minAmount) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException("from must be before or equal to to");
        }
        if (minAmount != null && minAmount.signum() < 0) {
            throw new IllegalArgumentException("minAmount cannot be negative");
        }
        List<BankTransaction> results = transactions.findAccountHistory(accountId, from, to, minAmount);
        if (!admin && results.stream().anyMatch(transaction -> !transaction.getCustomerId().equals(actorId))) {
            throw new AccessDeniedException("Access denied");
        }
        return results.stream().map(TransactionResponse::from).toList();
    }

    private void requireOwner(BankTransaction transaction, String actorId, boolean admin) {
        if (!admin && !transaction.getCustomerId().equals(actorId)) {
            throw new AccessDeniedException("Access denied");
        }
    }
}
