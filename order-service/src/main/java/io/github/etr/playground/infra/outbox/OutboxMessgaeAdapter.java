package io.github.etr.playground.infra.outbox;

import java.util.function.Function;

public interface OutboxMessgaeAdapter<T> {

    Class<T> domainEventType();

    String topic();

    String key(T domainEvent);

    String payload(T domainEvent);

    static <E> OutboxMessgaeAdapter<E> of(Class<E> domainEventType, String topic, Function<E, String> keyFunc, Function<E, String> adaptFunc) {
        return new OutboxMessageAdapterImpl<>(domainEventType, topic, keyFunc, adaptFunc);
    }

    record OutboxMessageAdapterImpl<T>(Class<T> domainEventType, String topic, Function<T, String> keyFunc, Function<T, String> payloadFunc)
        implements OutboxMessgaeAdapter<T> {

        @Override
        public String payload(T domainEvent) {
            return payloadFunc.apply(domainEvent);
        }

        @Override
        public String key(T domainEvent) {
            return keyFunc.apply(domainEvent);
        }
    }
}
