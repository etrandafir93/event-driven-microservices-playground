package io.github.etr.inbox;

public interface InboxMessageAdapter<T> {

    String topic();

    T domainEvent(String msgPayload);
}