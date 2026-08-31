package com.nexabank.customer.dto;

import com.nexabank.customer.entity.Customer;
import com.nexabank.customer.entity.CustomerStatus;

import java.time.Instant;

public record CustomerResponse(
        String id,
        String firstName,
        String lastName,
        String email,
        String phone,
        CustomerStatus status,
        Instant createdAt,
        Instant updatedAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(), customer.getFirstName(), customer.getLastName(),
                customer.getEmail(), customer.getPhone(), customer.getStatus(),
                customer.getCreatedAt(), customer.getUpdatedAt());
    }
}

