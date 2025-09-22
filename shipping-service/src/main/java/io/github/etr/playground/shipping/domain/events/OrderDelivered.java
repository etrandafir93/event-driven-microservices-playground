package io.github.etr.playground.shipping.domain.events;

import java.time.Instant;

import org.springframework.modulith.events.Externalized;

@Externalized("order-delivered")
public record OrderDelivered(
    String orderId,
    String username,
    String trackingNumber,
    String carrier,
    Instant shippedAt,
    Instant deliveredAt
) {
}