package com.nexabank.account.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class IdempotencyCache {
    private static final Duration TTL = Duration.ofHours(24);
    private final StringRedisTemplate redis;

    public IdempotencyCache(StringRedisTemplate redis) {
        this.redis = redis;
    }

    public CacheEntry get(String actorId, String idempotencyKey) {
        try {
            String value = redis.opsForValue().get(redisKey(actorId, idempotencyKey));
            if (value == null) return null;
            String[] parts = value.split("\\|", 3);
            if (parts.length != 3) return null;
            String responseJson = new String(Base64.getDecoder().decode(parts[2]), StandardCharsets.UTF_8);
            return new CacheEntry(parts[0], parts[1], responseJson);
        } catch (RuntimeException exception) {
            return null;
        }
    }

    public void putAfterCommit(String actorId, String idempotencyKey, String requestHash,
                               String operationType, String responseJson) {
        Runnable write = () -> put(actorId, idempotencyKey, requestHash, operationType, responseJson);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    write.run();
                }
            });
        } else {
            write.run();
        }
    }

    private void put(String actorId, String idempotencyKey, String requestHash,
                     String operationType, String responseJson) {
        try {
            String encoded = Base64.getEncoder().encodeToString(responseJson.getBytes(StandardCharsets.UTF_8));
            redis.opsForValue().set(redisKey(actorId, idempotencyKey),
                    requestHash + "|" + operationType + "|" + encoded, TTL);
        } catch (RuntimeException ignored) {
            // PostgreSQL remains the source of truth when Redis is unavailable.
        }
    }

    private String redisKey(String actorId, String idempotencyKey) {
        return "nexa:idempotency:" + actorId + ":" + idempotencyKey;
    }

    public record CacheEntry(String requestHash, String operationType, String responseJson) {
    }
}
