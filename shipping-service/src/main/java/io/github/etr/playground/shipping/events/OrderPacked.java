package io.github.etr.playground.shipping.events;

import java.time.Instant;

import org.springframework.modulith.events.Externalized;

@Externalized("order-packed")
public record OrderPacked(
    String orderId,
    String username,
    String trackingNumber,
    String carrier,
    Instant estimatedShipping,
    Instant estimatedDelivery
) {
}