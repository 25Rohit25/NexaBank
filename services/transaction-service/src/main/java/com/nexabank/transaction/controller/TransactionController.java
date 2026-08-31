package com.nexabank.transaction.controller;

import com.nexabank.transaction.dto.TransactionResponse;
import com.nexabank.transaction.service.TransactionQueryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionQueryService service;

    public TransactionController(TransactionQueryService service) {
        this.service = service;
    }

    @GetMapping("/{transactionId}")
    TransactionResponse get(@PathVariable String transactionId, @AuthenticationPrincipal Jwt jwt) {
        return service.get(transactionId, jwt.getSubject(), isAdmin(jwt));
    }

    @GetMapping("/account/{accountId}")
    List<TransactionResponse> accountHistory(@PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) BigDecimal minAmount, @AuthenticationPrincipal Jwt jwt) {
        return service.accountHistory(accountId, jwt.getSubject(), isAdmin(jwt), from, to, minAmount);
    }

    private boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("role"));
    }
}
