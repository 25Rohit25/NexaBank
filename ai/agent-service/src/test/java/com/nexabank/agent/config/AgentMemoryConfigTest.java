package com.nexabank.agent.config;

import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.convert.DurationStyle;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AgentMemoryConfigTest {
    private final AgentMemoryConfig config = new AgentMemoryConfig();

    @Test
    void keepsConversationMemoryIsolatedByAuthenticatedCustomerScope() {
        ChatMemory memory = config.agentChatMemory(new InMemoryChatMemoryRepository(), 20);

        memory.add("nexa-customer:CUS-1001", new UserMessage("Show my savings account"));
        memory.add("nexa-customer:CUS-1001", new AssistantMessage("Savings account selected"));
        memory.add("nexa-customer:CUS-2002", new UserMessage("Show my current account"));

        assertThat(memory.get("nexa-customer:CUS-1001"))
                .extracting(message -> message.getText())
                .containsExactly("Show my savings account", "Savings account selected");
        assertThat(memory.get("nexa-customer:CUS-2002"))
                .extracting(message -> message.getText())
                .containsExactly("Show my current account");
    }

    @Test
    void configuresRedisMemoryWithThirtyMinuteExpiry() throws IOException {
        StandardEnvironment environment = new StandardEnvironment();
        PropertySource<?> yaml = new YamlPropertySourceLoader()
                .load("agent", new ClassPathResource("application.yml"))
                .get(0);
        environment.getPropertySources().addFirst(yaml);

        String configuredTtl = environment.getRequiredProperty(
                "spring.ai.chat.memory.repository.redis.time-to-live");
        assertThat(DurationStyle.detectAndParse(configuredTtl))
                .isEqualTo(Duration.ofMinutes(30));
        assertThat(environment.getProperty("nexa.memory.max-messages", Integer.class))
                .isEqualTo(20);
    }

    @Test
    void rejectsUnboundedOrUselessMemoryWindows() {
        InMemoryChatMemoryRepository repository = new InMemoryChatMemoryRepository();

        assertThatThrownBy(() -> config.agentChatMemory(repository, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> config.agentChatMemory(repository, 101))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
