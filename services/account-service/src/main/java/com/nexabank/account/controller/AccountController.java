package com.nexabank.account.controller;

import com.nexabank.account.dto.AccountResponse;
import com.nexabank.account.dto.BalanceResponse;
import com.nexabank.account.dto.CreateAccountRequest;
import com.nexabank.account.dto.DepositRequest;
import com.nexabank.account.dto.DepositResponse;
import com.nexabank.account.service.AccountService;
import com.nexabank.account.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService service;
    private final BankingService bankingService;

    public AccountController(AccountService service, BankingService bankingService) {
        this.service = service;
        this.bankingService = bankingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AccountResponse create(@Valid @RequestBody CreateAccountRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.create(jwt.getSubject(), jwt.getTokenValue(), request);
    }

    @GetMapping("/{accountId}")
    AccountResponse get(@PathVariable String accountId, @AuthenticationPrincipal Jwt jwt) {
        return service.get(accountId, jwt.getSubject(), isAdmin(jwt));
    }

    @GetMapping("/customer/{customerId}")
    List<AccountResponse> getForCustomer(@PathVariable String customerId, @AuthenticationPrincipal Jwt jwt) {
        return service.getForCustomer(customerId, jwt.getSubject(), isAdmin(jwt));
    }

    @GetMapping("/{accountId}/balance")
    BalanceResponse getBalance(@PathVariable String accountId, @AuthenticationPrincipal Jwt jwt) {
        return service.getBalance(accountId, jwt.getSubject(), isAdmin(jwt));
    }

    @PostMapping("/{accountId}/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    DepositResponse deposit(@PathVariable String accountId, @Valid @RequestBody DepositRequest request,
                            @RequestHeader("Idempotency-Key") String idempotencyKey,
                            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
                            @AuthenticationPrincipal Jwt jwt) {
        return bankingService.deposit(accountId, request.amount(), jwt.getSubject(), isAdmin(jwt),
                idempotencyKey, correlationId == null ? UUID.randomUUID().toString() : correlationId);
    }

    private boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("role"));
    }
}
