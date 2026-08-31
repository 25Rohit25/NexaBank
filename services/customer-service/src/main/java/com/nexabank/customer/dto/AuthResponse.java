package com.nexabank.customer.dto;

public record AuthResponse(String accessToken, String tokenType, long expiresIn) {
}

