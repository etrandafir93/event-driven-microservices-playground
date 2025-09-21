package io.github.etr.playground.shipping.events;

import java.time.Instant;

public record OrderShipped(
    String orderId,
    String username,
    String trackingNumber,
    String carrier,
    Instant shippedAt,
    Instant estimatedDelivery
) {
}