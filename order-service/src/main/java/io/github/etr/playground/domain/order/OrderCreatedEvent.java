package io.github.etr.playground.domain.order;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

public record OrderCreatedEvent(String orderId, String username, Map<String, Integer> order) {

    public static OrderCreatedEvent from(Order order) {
        return new OrderCreatedEvent(order.orderId(),
        order.customerUsername(),
        order.orderItems()
            .stream()
            .collect(toMap(OrderItem::productSku, OrderItem::quantity))
        );
    }

}
