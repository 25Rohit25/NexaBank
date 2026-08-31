package com.nexabank.account.controller;

import com.nexabank.account.dto.TransferRequest;
import com.nexabank.account.dto.TransferResponse;
import com.nexabank.account.service.BankingService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {
    private final BankingService bankingService;

    public TransferController(BankingService bankingService) {
        this.bankingService = bankingService;
    }

    @PostMapping
    TransferResponse transfer(@Valid @RequestBody TransferRequest request,
                              @RequestHeader("Idempotency-Key") String idempotencyKey,
                              @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId,
                              @AuthenticationPrincipal Jwt jwt) {
        return bankingService.transfer(request, jwt.getSubject(), isAdmin(jwt), idempotencyKey,
                correlationId == null ? UUID.randomUUID().toString() : correlationId);
    }

    private boolean isAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("role"));
    }
}
