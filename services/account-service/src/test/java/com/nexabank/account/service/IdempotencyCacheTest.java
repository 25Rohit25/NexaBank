package com.nexabank.account.service;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IdempotencyCacheTest {
    @Test
    void cachedResponseRoundTripsAndExpiresAfterOneDay() {
        StringRedisTemplate redis = mock(StringRedisTemplate.class);
        @SuppressWarnings("unchecked")
        ValueOperations<String, String> values = mock(ValueOperations.class);
        when(redis.opsForValue()).thenReturn(values);
        IdempotencyCache cache = new IdempotencyCache(redis);

        cache.putAfterCommit("CUS-1", "key-1", "hash-1", "TRANSFER", "{\"status\":\"COMPLETED\"}");

        verify(values).set(anyString(), anyString(), org.mockito.ArgumentMatchers.eq(Duration.ofHours(24)));
        when(values.get("nexa:idempotency:CUS-1:key-1"))
                .thenReturn("hash-1|TRANSFER|eyJzdGF0dXMiOiJDT01QTEVURUQifQ==");
        IdempotencyCache.CacheEntry entry = cache.get("CUS-1", "key-1");
        assertThat(entry.requestHash()).isEqualTo("hash-1");
        assertThat(entry.responseJson()).isEqualTo("{\"status\":\"COMPLETED\"}");
    }
}
