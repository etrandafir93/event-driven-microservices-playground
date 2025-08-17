package io.github.etr.playground.application.inbox;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
public class InboxMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topic;
    private String key;
    private String payload;
    private Instant processedAt;
    private Status status = Status.PENDING;
    private final Instant observedAt = Instant.now();

    enum Status {
        PENDING,
        PROCESSED_OK,
        RETRYABLE_ERROR,
        NON_RETRYABLE_ERROR
    }
}
