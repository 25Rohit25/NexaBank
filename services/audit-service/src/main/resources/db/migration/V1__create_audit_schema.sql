CREATE TABLE audit_records (
    event_id VARCHAR(36) PRIMARY KEY,
    actor_id VARCHAR(32) NOT NULL,
    action VARCHAR(64) NOT NULL,
    resource_type VARCHAR(32) NOT NULL,
    resource_id VARCHAR(36) NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL,
    request_id VARCHAR(64) NOT NULL
);

CREATE INDEX idx_audit_actor_time ON audit_records (actor_id, occurred_at DESC);
CREATE INDEX idx_audit_resource ON audit_records (resource_type, resource_id);
CREATE INDEX idx_audit_request ON audit_records (request_id);
