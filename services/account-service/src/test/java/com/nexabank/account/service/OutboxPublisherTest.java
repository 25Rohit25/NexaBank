package com.nexabank.account.service;

import com.nexabank.account.entity.OutboxEvent;
import com.nexabank.account.repository.OutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxPublisherTest {
    @Test
    void marksEventPublishedOnlyAfterKafkaAcknowledgesIt() throws Exception {
        OutboxEventRepository repository = mock(OutboxEventRepository.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafka = mock(KafkaTemplate.class);
        OutboxEvent event = new OutboxEvent("evt-1", "transfer-1", "bank.transfer.completed", "{}", Instant.now());
        when(repository.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()).thenReturn(List.of(event));
        when(kafka.send("bank.transfer.completed", "transfer-1", "{}"))
                .thenReturn(CompletableFuture.completedFuture(null));

        new OutboxPublisher(repository, kafka).publishPending();

        verify(kafka).send("bank.transfer.completed", "transfer-1", "{}");
        assertThat(event.getPublishedAt()).isNotNull();
    }
}
