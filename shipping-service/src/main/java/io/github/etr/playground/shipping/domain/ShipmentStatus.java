package io.github.etr.playground.shipping.domain;

import static io.github.etr.playground.shipping.domain.OrderShipmentCommands.Deliver;
import static io.github.etr.playground.shipping.domain.OrderShipmentCommands.Pack;
import static io.github.etr.playground.shipping.domain.OrderShipmentCommands.Ship;

enum ShipmentStatus {
    NEW,
    PACKED,
    SHIPPED,
    DELIVERED;

    static ShipmentStatus afterShippingUpdate(OrderShipmentCommands update) {
        return switch (update) {
            case Deliver __ -> ShipmentStatus.DELIVERED;
            case Pack __ -> ShipmentStatus.PACKED;
            case Ship __ -> ShipmentStatus.SHIPPED;
        };
    }
}