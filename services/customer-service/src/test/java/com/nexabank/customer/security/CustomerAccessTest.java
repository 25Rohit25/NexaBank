package com.nexabank.customer.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerAccessTest {

    private final CustomerAccess access = new CustomerAccess();

    @Test
    void customerCanOnlyAccessOwnRecord() {
        Jwt jwt = jwt("CUS-1001", "CUSTOMER");

        assertThat(access.canAccess(jwt, "CUS-1001")).isTrue();
        assertThat(access.canAccess(jwt, "CUS-9999")).isFalse();
    }

    @Test
    void adminCanAccessAnyRecord() {
        assertThat(access.canAccess(jwt("ADMIN-1", "ADMIN"), "CUS-9999")).isTrue();
    }

    @Test
    void emailLookupIsAuthorizedBeforeRepositoryAccess() {
        Jwt jwt = jwt("CUS-1001", "CUSTOMER", "owner@example.com");

        assertThat(access.canAccessEmail(jwt, "owner@example.com")).isTrue();
        assertThat(access.canAccessEmail(jwt, "another@example.com")).isFalse();
    }

    private Jwt jwt(String subject, String role) {
        return jwt(subject, role, "customer@example.com");
    }

    private Jwt jwt(String subject, String role, String email) {
        Instant now = Instant.now();
        return new Jwt("token", now, now.plusSeconds(60), Map.of("alg", "none"),
                Map.of("sub", subject, "role", role, "email", email));
    }
}
