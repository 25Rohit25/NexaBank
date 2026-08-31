package com.nexabank.agent.controller;

import com.nexabank.agent.dto.ChatRequest;
import com.nexabank.agent.dto.ChatResponse;
import com.nexabank.agent.service.AgentChatService;
import jakarta.validation.Valid;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agent")
public class AgentController {
    private final AgentChatService agentChatService;

    public AgentController(AgentChatService agentChatService) {
        this.agentChatService = agentChatService;
    }

    @PostMapping("/chat")
    ChatResponse chat(@Valid @RequestBody ChatRequest request, JwtAuthenticationToken authentication) {
        return new ChatResponse(agentChatService.chat(
                authentication.getToken().getSubject(),
                authentication.getToken().getTokenValue(),
                request.message()));
    }
}
