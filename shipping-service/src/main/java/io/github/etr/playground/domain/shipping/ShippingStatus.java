package io.github.etr.playground.domain.shipping;

import static io.github.etr.playground.domain.shipping.ShippingUpdate.Delivery;
import static io.github.etr.playground.domain.shipping.ShippingUpdate.Packing;
import static io.github.etr.playground.domain.shipping.ShippingUpdate.Shipping;

public enum ShippingStatus {
    NEW,
    PACKED,
    SHIPPED,
    DELIVERED;

    public static ShippingStatus afterShippingUpdate(ShippingUpdate update) {
        return switch (update) {
            case Delivery __ -> ShippingStatus.DELIVERED;
            case Packing __ -> ShippingStatus.PACKED;
            case Shipping __ -> ShippingStatus.SHIPPED;
        };
    }
}