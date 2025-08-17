package io.github.etr.playground.infra;

import java.time.Instant;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.github.etr.playground.domain.OrderService;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class OrderShippedListener {

    private final OrderService orders;

    @KafkaListener(topics = "order-shipped")
    void onOrderShipped(OrderShippedEvent event) {
        orders.orderShipped(event.orderId(), event.username());
    }

    record OrderShippedEvent(
        String orderId,
        String username,
        String trackingNumber,
        String carrier,
        Instant shippedAt,
        Instant estimatedDelivery
    ) {}

}
