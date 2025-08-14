package io.github.etr.playground.application.outbox;

public interface OutboxEvent {
    String key();
    String topic();
}
