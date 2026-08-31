package com.nexabank.customer.service;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CustomerIdGenerator {
    public String nextId() {
        return "CUS-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}

