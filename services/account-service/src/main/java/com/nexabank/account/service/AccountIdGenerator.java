package com.nexabank.account.service;

import com.nexabank.account.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

@Component
public class AccountIdGenerator {

    private final SecureRandom random = new SecureRandom();
    private final AccountRepository repository;

    public AccountIdGenerator(AccountRepository repository) {
        this.repository = repository;
    }

    public String nextId() {
        return "ACC-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    public String nextAccountNumber() {
        String candidate;
        do {
            candidate = "91" + String.format("%010d", random.nextLong(10_000_000_000L));
        } while (repository.existsByAccountNumber(candidate));
        return candidate;
    }
}

