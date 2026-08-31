package com.nexabank.audit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "audit_records")
public class AuditRecord {
    @Id
    @Column(name = "event_id", length = 36, nullable = false, updatable = false)
    private String eventId;
    @Column(name = "actor_id", length = 32, nullable = false, updatable = false)
    private String actorId;
    @Column(length = 64, nullable = false, updatable = false)
    private String action;
    @Column(name = "resource_type", length = 32, nullable = false, updatable = false)
    private String resourceType;
    @Column(name = "resource_id", length = 36, nullable = false, updatable = false)
    private String resourceId;
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
    @Column(length = 20, nullable = false, updatable = false)
    private String status;
    @Column(name = "request_id", length = 64, nullable = false, updatable = false)
    private String requestId;

    protected AuditRecord() {}

    public AuditRecord(String eventId, String actorId, String action, String resourceType,
                       String resourceId, Instant occurredAt, String status, String requestId) {
        this.eventId = eventId;
        this.actorId = actorId;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.occurredAt = occurredAt;
        this.status = status;
        this.requestId = requestId;
    }

    public String getEventId() { return eventId; }
    public String getActorId() { return actorId; }
    public String getAction() { return action; }
    public String getResourceId() { return resourceId; }
}
