package com.nexabank.customer.controller;

import com.nexabank.customer.dto.CreateCustomerRequest;
import com.nexabank.customer.dto.CustomerResponse;
import com.nexabank.customer.dto.UpdateCustomerRequest;
import com.nexabank.customer.service.CustomerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CustomerResponse create(@Valid @RequestBody CreateCustomerRequest request) {
        return service.create(request);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@customerAccess.canAccess(#jwt, #id)")
    CustomerResponse get(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        return service.get(id);
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("@customerAccess.canAccessEmail(#jwt, #email)")
    CustomerResponse getByEmail(@PathVariable @Email String email, @AuthenticationPrincipal Jwt jwt) {
        return service.getByEmail(email);
    }

    @PutMapping("/{id}")
    @PreAuthorize("@customerAccess.canAccess(#jwt, #id)")
    CustomerResponse update(@PathVariable String id,
                            @Valid @RequestBody UpdateCustomerRequest request,
                            @AuthenticationPrincipal Jwt jwt) {
        return service.update(id, request);
    }
}
