package com.nexabank.agent.service;

import com.nexabank.agent.mcp.AuthenticatedMcpClientFactory;
import com.nexabank.agent.mcp.AuthenticatedMcpSession;
import com.nexabank.agent.rag.PolicySearchRequestFactory;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaChatOptions;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AgentChatServiceHybridTest {
    @Test
    void combinesAuthenticatedMcpToolsAndPolicyEvidenceInOneAgentTurn() {
        String question = "Can I transfer INR 50,000 internationally, and what will it cost?";
        ChatModel chatModel = mock(ChatModel.class);
        ChatMemory chatMemory = mock(ChatMemory.class);
        VectorStore vectorStore = mock(VectorStore.class);
        AuthenticatedMcpClientFactory mcpFactory = mock(AuthenticatedMcpClientFactory.class);
        AuthenticatedMcpSession mcpSession = mock(AuthenticatedMcpSession.class);
        ToolCallbackProvider tools = mock(ToolCallbackProvider.class);

        when(chatMemory.get("nexa-customer:CUS-1001")).thenReturn(List.of());
        when(chatModel.getOptions()).thenReturn(OllamaChatOptions.builder().build());
        when(vectorStore.similaritySearch(any(SearchRequest.class))).thenReturn(List.of(
                new Document(
                        "International transfers are not executable in Nexa Bank V1.",
                        Map.of("documentType", "bank_policy", "policyId", "international-transfer-policy"))));
        when(mcpFactory.open("signed-token")).thenReturn(mcpSession);
        when(mcpSession.tools()).thenReturn(tools);
        when(tools.getToolCallbacks()).thenReturn(new org.springframework.ai.tool.ToolCallback[0]);
        when(chatModel.call(any(Prompt.class))).thenReturn(new ChatResponse(List.of(
                new Generation(new AssistantMessage("Live balance and policy were checked.")))));

        QuestionAnswerAdvisor policyAdvisor = QuestionAnswerAdvisor.builder(vectorStore)
                .searchRequest(new PolicySearchRequestFactory(4, 0.72).create())
                .build();
        AgentChatService service = new AgentChatService(
                ChatClient.builder(chatModel), chatMemory, policyAdvisor,
                new AgentPromptFactory(), mcpFactory);

        String answer = service.chat("CUS-1001", "signed-token", question);

        assertThat(answer).isEqualTo("Live balance and policy were checked.");
        verify(mcpFactory).open("signed-token");
        verify(tools).getToolCallbacks();
        verify(mcpSession).close();
        ArgumentCaptor<SearchRequest> search = ArgumentCaptor.forClass(SearchRequest.class);
        verify(vectorStore).similaritySearch(search.capture());
        assertThat(search.getValue().getQuery()).isEqualTo(question);
        assertThat(search.getValue().getFilterExpression().toString())
                .contains("documentType", "bank_policy");

        ArgumentCaptor<Prompt> augmentedPrompt = ArgumentCaptor.forClass(Prompt.class);
        verify(chatModel).call(augmentedPrompt.capture());
        assertThat(augmentedPrompt.getValue().getUserMessage().getText())
                .contains(question)
                .contains("International transfers are not executable in Nexa Bank V1.");
    }
}
