package io.github.etr.playground.infra.inbox;

public interface InboxMessageAdapter<T> {

    String topic();

    T domainEvent(String msgPayload);
}