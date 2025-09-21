package io.github.etr.playground.shipping.domain;

import java.time.Instant;

public sealed interface ShippingUpdate permits ShippingUpdate.Packing, ShippingUpdate.Shipping, ShippingUpdate.Delivery {

    record Packing(Instant estimatedShipping) implements ShippingUpdate {
    }

    record Shipping(Instant shippedAt, Instant estimatedDelivery) implements ShippingUpdate {
    }

    record Delivery(Instant deliveredAt) implements ShippingUpdate {
    }

}
