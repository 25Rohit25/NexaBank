package com.nexabank.mcp.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthenticatedUserTest {
    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void returnsSubjectAndRawBearerTokenFromVerifiedJwtAuthentication() {
        Jwt jwt = Jwt.withTokenValue("signed-token").header("alg", "HS256").subject("CUS-1001")
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        AuthenticatedUser user = new AuthenticatedUser();
        assertThat(user.customerId()).isEqualTo("CUS-1001");
        assertThat(user.bearerToken()).isEqualTo("signed-token");
    }

    @Test
    void rejectsToolCallsWithoutAuthenticatedContext() {
        assertThatThrownBy(() -> new AuthenticatedUser().customerId())
                .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }
}
