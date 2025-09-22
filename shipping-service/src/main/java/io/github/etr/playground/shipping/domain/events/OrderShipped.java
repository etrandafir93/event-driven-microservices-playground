package io.github.etr.playground.shipping.domain.events;

import java.time.Instant;

import org.springframework.modulith.events.Externalized;

@Externalized("order-shipped")
public record OrderShipped(
    String orderId,
    String username,
    String trackingNumber,
    String carrier,
    Instant shippedAt,
    Instant estimatedDelivery
) {
}