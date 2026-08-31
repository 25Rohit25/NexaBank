package com.nexabank.transaction.repository;

import com.nexabank.transaction.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, String>,
        JpaSpecificationExecutor<BankTransaction> {
}
