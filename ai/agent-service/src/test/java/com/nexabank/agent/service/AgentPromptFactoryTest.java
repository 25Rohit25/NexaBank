package com.nexabank.agent.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AgentPromptFactoryTest {
    private final AgentPromptFactory promptFactory = new AgentPromptFactory();

    @Test
    void bindsTheAuthenticatedCustomerAndDeterministicSafetyRules() {
        String prompt = promptFactory.systemPrompt("CUS-1001");

        assertThat(prompt)
                .contains("CUS-1001")
                .contains("only authority")
                .contains("explicitly confirmed")
                .contains("Never invent banking data");
    }
}
