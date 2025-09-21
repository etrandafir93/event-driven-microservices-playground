package io.github.etr.playground;

import java.time.Instant;

public record OrderDelivered(
    String orderId,
    String username,
    String trackingNumber,
    String carrier,
    Instant shippedAt,
    Instant deliveredAt
) {
}