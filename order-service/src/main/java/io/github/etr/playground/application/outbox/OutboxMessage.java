package io.github.etr.playground.application.outbox;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json")
    private Map<String, String> headers = new HashMap<>();

    public OutboxMessage originalTraceId(String traceId) {
        this.headers.put("originalTraceId", traceId);
        return this;
    }

    public String originalTraceId() {
        return headers.getOrDefault("originalTraceId", "");
    }

}