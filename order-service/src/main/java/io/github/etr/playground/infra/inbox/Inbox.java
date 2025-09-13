package io.github.etr.playground.infra.inbox;

import java.util.UUID;

@FunctionalInterface
public interface Inbox {

    void uniqueIncomingMessage(String topic, String key, String json, String idempotencyKey);

    default void incomingMessage(String topic, String key, String json) {
        uniqueIncomingMessage(topic, key, json, UUID.randomUUID()
            .toString());
    }

}