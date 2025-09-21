package io.github.etr.playground;

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