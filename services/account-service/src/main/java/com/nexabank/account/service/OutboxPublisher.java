package com.nexabank.account.service;

import com.nexabank.account.entity.OutboxEvent;
import com.nexabank.account.repository.OutboxEventRepository;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class OutboxPublisher {
    private final OutboxEventRepository outboxEvents;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OutboxPublisher(OutboxEventRepository outboxEvents, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxEvents = outboxEvents;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${outbox.publish-delay-ms:1000}")
    @Transactional
    public void publishPending() throws Exception {
        for (OutboxEvent event : outboxEvents.findTop100ByPublishedAtIsNullOrderByCreatedAtAsc()) {
            kafkaTemplate.send(event.getEventType(), event.getAggregateId(), event.getPayload())
                    .get(10, TimeUnit.SECONDS);
            event.markPublished(Instant.now());
        }
    }
}
