package io.github.etr.playground.infra.inbox;

import java.time.Instant;
import java.util.UUID;

@FunctionalInterface
public interface Inbox {

    void incomingMessage(String topic, String key, String json, String idempotencyKey, Instant observedAt);

    default void incomingMessage(String topic, String key, String json) {
        incomingMessage(topic, key, json, UUID.randomUUID()
            .toString(), Instant.now());
    }

}