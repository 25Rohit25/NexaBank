package com.nexabank.account.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {
    @Id
    @Column(length = 36, nullable = false, updatable = false)
    private String id;

    @Column(name = "aggregate_id", length = 36, nullable = false, updatable = false)
    private String aggregateId;

    @Column(name = "event_type", length = 64, nullable = false, updatable = false)
    private String eventType;

    @Column(columnDefinition = "text", nullable = false, updatable = false)
    private String payload;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "published_at")
    private Instant publishedAt;

    protected OutboxEvent() {}

    public OutboxEvent(String id, String aggregateId, String eventType, String payload, Instant createdAt) {
        this.id = id;
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.payload = payload;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getAggregateId() { return aggregateId; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public void markPublished(Instant at) { this.publishedAt = at; }
}
