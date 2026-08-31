package com.nexabank.mcp.security;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUser {
    public String customerId() {
        return authentication().getToken().getSubject();
    }

    public String bearerToken() {
        return authentication().getToken().getTokenValue();
    }

    private JwtAuthenticationToken authentication() {
        if (SecurityContextHolder.getContext().getAuthentication() instanceof JwtAuthenticationToken jwt) {
            return jwt;
        }
        throw new AuthenticationCredentialsNotFoundException("Authenticated customer context is required");
    }
}
