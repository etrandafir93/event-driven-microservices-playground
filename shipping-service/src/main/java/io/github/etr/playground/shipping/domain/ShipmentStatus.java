package io.github.etr.playground.shipping.domain;

import static io.github.etr.playground.shipping.domain.ShippingUpdate.Delivery;
import static io.github.etr.playground.shipping.domain.ShippingUpdate.Packing;
import static io.github.etr.playground.shipping.domain.ShippingUpdate.Shipping;

enum ShipmentStatus {
    NEW,
    PACKED,
    SHIPPED,
    DELIVERED;

    static ShipmentStatus afterShippingUpdate(ShippingUpdate update) {
        return switch (update) {
            case Delivery __ -> ShipmentStatus.DELIVERED;
            case Packing __ -> ShipmentStatus.PACKED;
            case Shipping __ -> ShipmentStatus.SHIPPED;
        };
    }
}