package com.nexabank.customer.service;

import com.nexabank.customer.dto.AuthResponse;
import com.nexabank.customer.entity.Credential;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final Duration ttl;

    public JwtService(JwtEncoder encoder,
                      @Value("${security.jwt.issuer}") String issuer,
                      @Value("${security.jwt.ttl}") Duration ttl) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.ttl = ttl;
    }

    public AuthResponse issue(Credential credential) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(credential.getCustomerId())
                .claim("email", credential.getEmail())
                .claim("role", credential.getRole().name())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        return new AuthResponse(token, "Bearer", ttl.toSeconds());
    }
}
