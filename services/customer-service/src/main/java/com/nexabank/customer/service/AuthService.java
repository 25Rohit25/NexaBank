package com.nexabank.customer.service;

import com.nexabank.customer.dto.AuthResponse;
import com.nexabank.customer.dto.LoginRequest;
import com.nexabank.customer.dto.RegisterRequest;
import com.nexabank.customer.entity.Credential;
import com.nexabank.customer.entity.Customer;
import com.nexabank.customer.entity.Role;
import com.nexabank.customer.exception.ConflictException;
import com.nexabank.customer.repository.CredentialRepository;
import com.nexabank.customer.repository.CustomerRepository;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final CustomerRepository customerRepository;
    private final CredentialRepository credentialRepository;
    private final CustomerIdGenerator idGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(CustomerRepository customerRepository,
                       CredentialRepository credentialRepository,
                       CustomerIdGenerator idGenerator,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.customerRepository = customerRepository;
        this.credentialRepository = credentialRepository;
        this.idGenerator = idGenerator;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String email = CustomerService.normalizeEmail(request.email());
        if (customerRepository.existsByEmailIgnoreCase(email)) {
            throw new ConflictException("A customer with this email already exists");
        }
        Customer customer = customerRepository.save(new Customer(idGenerator.nextId(),
                request.firstName().trim(), request.lastName().trim(), email, request.phone().trim()));
        Credential credential = credentialRepository.save(new Credential(customer.getId(), email,
                passwordEncoder.encode(request.password()), Role.CUSTOMER));
        return jwtService.issue(credential);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Credential credential = credentialRepository.findByEmailIgnoreCase(CustomerService.normalizeEmail(request.email()))
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), credential.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return jwtService.issue(credential);
    }
}

