package io.github.etr.playground.shipping.domain;

import java.time.Instant;

import jakarta.persistence.Embeddable;

public sealed interface ShippingUpdate permits ShippingUpdate.Packing, ShippingUpdate.Shipping, ShippingUpdate.Delivery {

    @Embeddable
    record Packing(Instant estimatedShipping, Instant estimatedDelivery) implements ShippingUpdate {
    }

    @Embeddable
    record Shipping(Instant shippedAt, Instant estimatedDelivery) implements ShippingUpdate {
    }

    @Embeddable
    record Delivery(Instant shippedAt, Instant deliveredAt) implements ShippingUpdate {
    }

}
