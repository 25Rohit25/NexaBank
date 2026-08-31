package com.nexabank.customer.service;

import com.nexabank.customer.dto.CreateCustomerRequest;
import com.nexabank.customer.dto.CustomerResponse;
import com.nexabank.customer.dto.UpdateCustomerRequest;
import com.nexabank.customer.entity.Customer;
import com.nexabank.customer.exception.ConflictException;
import com.nexabank.customer.exception.ResourceNotFoundException;
import com.nexabank.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerIdGenerator idGenerator;

    public CustomerService(CustomerRepository repository, CustomerIdGenerator idGenerator) {
        this.repository = repository;
        this.idGenerator = idGenerator;
    }

    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        String email = normalizeEmail(request.email());
        if (repository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A customer with this email already exists");
        }
        Customer customer = new Customer(idGenerator.nextId(), request.firstName().trim(),
                request.lastName().trim(), email, request.phone().trim());
        return CustomerResponse.from(repository.save(customer));
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(String id) {
        return CustomerResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public CustomerResponse getByEmail(String email) {
        return repository.findByEmailIgnoreCase(normalizeEmail(email))
                .map(CustomerResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
    }

    @Transactional
    public CustomerResponse update(String id, UpdateCustomerRequest request) {
        Customer customer = find(id);
        customer.update(request.firstName().trim(), request.lastName().trim(), request.phone().trim());
        return CustomerResponse.from(customer);
    }

    Customer find(String id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + id));
    }

    static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

