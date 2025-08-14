package io.github.etr.playground.domain;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

import io.github.etr.playground.application.outbox.OutboxEvent;

public record OrderCreatedEvent(String orderId, String customerUsername, Map<String, Integer> order)
    implements OutboxEvent {

    // now domain depends on app layer.
    // if we want to be really strict about it,
    // we can remove this dependency by externalizing this config to yml
    // eg: outbox.OrderCreatedEvent.topic/key/headers = ...

    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order.orderId(),
        order.customerUsername(),
        order.orderItems()
            .stream()
            .collect(toMap(OrderItem::productSku, OrderItem::quantity))
        );
    }

    @Override
    public String key() {
        return orderId;
    }

    @Override
    public String topic() {
        return "order-created";
    }
}
