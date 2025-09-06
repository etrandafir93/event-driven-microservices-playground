package io.github.etr.playground.infra;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.etr.playground.application.annotations.Adapter;
import io.github.etr.playground.domain.order.OrderCreatedEvent;
import io.github.etr.playground.infra.outbox.OutboxMessgaeAdapter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@Adapter
@RequiredArgsConstructor
class OrderCreatedProducer implements OutboxMessgaeAdapter<OrderCreatedEvent> {

    private static final String TOPIC = "order-created";

    private final ObjectMapper mapper;

    @Override
    public Class<OrderCreatedEvent> domainEventType() {
        return OrderCreatedEvent.class;
    }

    @Override
    public String topic() {
        return TOPIC;
    }

    @Override
    public String key(OrderCreatedEvent domainEvent) {
        return domainEvent.orderId();
    }

    @SneakyThrows
    @Override
    public String payload(OrderCreatedEvent domainEvent) {
        return mapper.writeValueAsString(domainEvent);
    }

}
