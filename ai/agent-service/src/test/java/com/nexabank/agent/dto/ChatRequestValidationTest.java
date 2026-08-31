package com.nexabank.agent.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRequestValidationTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void rejectsBlankMessages() {
        assertThat(validator.validate(new ChatRequest("   "))).isNotEmpty();
    }

    @Test
    void rejectsMessagesOverTwoThousandCharacters() {
        assertThat(validator.validate(new ChatRequest("x".repeat(2001)))).isNotEmpty();
    }
}
