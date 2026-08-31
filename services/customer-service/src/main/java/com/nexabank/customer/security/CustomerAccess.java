package com.nexabank.customer.security;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("customerAccess")
public class CustomerAccess {
    public boolean canAccess(Jwt jwt, String customerId) {
        return customerId.equals(jwt.getSubject()) || "ADMIN".equals(jwt.getClaimAsString("role"));
    }

    public boolean canAccessEmail(Jwt jwt, String email) {
        return email.equalsIgnoreCase(jwt.getClaimAsString("email"))
                || "ADMIN".equals(jwt.getClaimAsString("role"));
    }
}
