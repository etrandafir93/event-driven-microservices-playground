package io.github.etr.playground.application.inbox;

public interface InboxMessageAdapter<T> {

    String topic();

    T adapt(String inboxMsg);
}
