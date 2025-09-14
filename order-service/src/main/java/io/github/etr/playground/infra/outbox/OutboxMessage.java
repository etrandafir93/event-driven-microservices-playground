package io.github.etr.playground.infra.outbox;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
class OutboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topic;
    private String key;
    private String payload;
    private String eventType;
    private Instant observedAt;
    private Instant publishedAt;
    @ElementCollection
    private List<Header> headers = new ArrayList<>();

    @Data
    @Embeddable
    @NoArgsConstructor
    @AllArgsConstructor
    static class Header {
        private String key;
        private String value;
    }

    public OutboxMessage originalTraceId(String traceId) {
        this.headers.add(new Header("originalTraceId", traceId));
        return this;
    }

    public OutboxMessage idempotencyKey(String idempotencyKey) {
        this.headers.add(new Header("idempotencyKey", idempotencyKey));
        return this;
    }

}