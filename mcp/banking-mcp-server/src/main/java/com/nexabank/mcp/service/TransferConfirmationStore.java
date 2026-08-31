package com.nexabank.mcp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class TransferConfirmationStore {
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public TransferConfirmationStore(StringRedisTemplate redis, ObjectMapper objectMapper,
                                     @Value("${banking.transfer-confirmation-ttl:5m}") Duration ttl) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    public PendingTransfer create(String customerId, String sourceAccountId,
                                  String destinationAccountId, BigDecimal amount) {
        String token = UUID.randomUUID().toString();
        PendingTransfer transfer = new PendingTransfer(token, customerId, sourceAccountId,
                destinationAccountId, amount.setScale(2), Instant.now().plus(ttl));
        try {
            redis.opsForValue().set(key(token), objectMapper.writeValueAsString(transfer), ttl);
            return transfer;
        } catch (tools.jackson.core.JacksonException exception) {
            throw new IllegalStateException("Could not store transfer confirmation", exception);
        }
    }

    public PendingTransfer require(String token, String customerId) {
        String json = redis.opsForValue().get(key(token));
        if (json == null) throw new IllegalArgumentException("Transfer confirmation is missing or expired");
        try {
            PendingTransfer transfer = objectMapper.readValue(json, PendingTransfer.class);
            if (!transfer.customerId().equals(customerId)) {
                throw new IllegalArgumentException("Transfer confirmation belongs to another customer");
            }
            return transfer;
        } catch (tools.jackson.core.JacksonException exception) {
            throw new IllegalStateException("Stored transfer confirmation is invalid", exception);
        }
    }

    public void complete(String token) {
        redis.delete(key(token));
    }

    private String key(String token) {
        return "nexa:mcp:transfer-confirmation:" + token;
    }

    public record PendingTransfer(String token, String customerId, String sourceAccountId,
                                  String destinationAccountId, BigDecimal amount, Instant expiresAt) {
    }
}
