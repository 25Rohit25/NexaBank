package com.nexabank.account.exception;

import org.springframework.http.HttpStatus;

public class CustomerValidationException extends RuntimeException {
    private final HttpStatus status;

    public CustomerValidationException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}

