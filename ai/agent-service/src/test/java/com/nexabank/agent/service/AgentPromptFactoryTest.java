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
                .contains("Never invent banking data")
                .contains("only retrieved bank_policy evidence")
                .contains("I couldn't find this information in the available banking policies");
    }

    @Test
    void scopesConversationMemoryToTheAuthenticatedCustomer() {
        assertThat(promptFactory.conversationId("CUS-1001"))
                .isEqualTo("nexa-customer:CUS-1001");
    }
}
