package com.nexabank.agent.controller;

import com.nexabank.agent.dto.ChatRequest;
import com.nexabank.agent.dto.ChatResponse;
import com.nexabank.agent.service.AgentChatService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentControllerTest {
    @Test
    void passesVerifiedJwtSubjectToTheAgentService() {
        AgentChatService service = mock(AgentChatService.class);
        when(service.chat("CUS-1001", "signed-token", "What is my balance?"))
                .thenReturn("Your balance is available.");
        AgentController controller = new AgentController(service);
        Jwt jwt = Jwt.withTokenValue("signed-token")
                .header("alg", "HS256")
                .subject("CUS-1001")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();

        ChatResponse response = controller.chat(
                new ChatRequest("What is my balance?"), new JwtAuthenticationToken(jwt));

        assertThat(response.message()).isEqualTo("Your balance is available.");
        verify(service).chat("CUS-1001", "signed-token", "What is my balance?");
    }
}
