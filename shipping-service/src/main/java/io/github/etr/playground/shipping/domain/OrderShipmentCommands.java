package io.github.etr.playground.shipping.domain;

import java.time.Instant;

public sealed interface OrderShipmentCommands permits OrderShipmentCommands.Pack, OrderShipmentCommands.Ship, OrderShipmentCommands.Deliver {

    record Pack(Instant packedAt, Instant estimatedShipping) implements OrderShipmentCommands {
    }

    record Ship(Instant shippedAt, Instant estimatedDelivery) implements OrderShipmentCommands {
    }

    record Deliver(Instant deliveredAt) implements OrderShipmentCommands {
    }

}
