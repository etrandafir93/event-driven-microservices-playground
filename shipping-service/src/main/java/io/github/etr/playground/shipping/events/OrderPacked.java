package io.github.etr.playground.shipping.events;

import java.time.Instant;

public record OrderPacked(
    String orderId,
    String username,
    String trackingNumber,
    String carrier,
    Instant estimatedShipping,
    Instant estimatedDelivery
) {
}