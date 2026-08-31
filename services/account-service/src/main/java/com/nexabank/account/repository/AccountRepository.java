package com.nexabank.account.repository;

import com.nexabank.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, String> {
    List<Account> findAllByCustomerIdOrderByCreatedAtAsc(String customerId);
    boolean existsByCustomerIdAndAccountType(String customerId, com.nexabank.account.entity.AccountType type);
    boolean existsByAccountNumber(String accountNumber);
}

