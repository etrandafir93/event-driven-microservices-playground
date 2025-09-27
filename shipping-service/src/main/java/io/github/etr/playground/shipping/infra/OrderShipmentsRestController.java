package io.github.etr.playground.shipping.infra;

import static io.github.etr.playground.shipping.infra.OrderShipmentLinks.linkDeliver;
import static io.github.etr.playground.shipping.infra.OrderShipmentLinks.linkSelf;
import static io.github.etr.playground.shipping.infra.OrderShipmentLinks.linkShip;

import java.time.Instant;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.etr.playground.shipping.domain.OrderShipmentsCommandHandler;
import io.github.etr.playground.shipping.domain.OrderShipmentQueries;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Deliver;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Pack;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Ship;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/shipments")
class OrderShipmentsRestController {

    private final OrderShipmentsCommandHandler commands;
    private final OrderShipmentQueries queries;

    @PutMapping("/{trackingNumber}/pack")
    OrderShipmentStatusUpdateResponse orderPacked(
        @PathVariable String trackingNumber,
        @RequestParam Instant packedAt,
        @RequestParam Instant estimatedShippingDate
    ) {
        var update = new Pack(packedAt, estimatedShippingDate);
        commands.updateShipmentStatus(trackingNumber, update);
        return new OrderShipmentStatusUpdateResponse(trackingNumber, update);
    }

    @PutMapping("/{trackingNumber}/ship")
    OrderShipmentStatusUpdateResponse orderShipped(
        @PathVariable String trackingNumber,
        @RequestParam Instant shippedAt,
        @RequestParam Instant estimatedDeliveryDate
    ) {
        var update = new Ship(shippedAt, estimatedDeliveryDate);
        commands.updateShipmentStatus(trackingNumber, update);
        return new OrderShipmentStatusUpdateResponse(trackingNumber, update);
    }

    @PutMapping("/{trackingNumber}/deliver")
    OrderShipmentStatusUpdateResponse orderDelivered(
        @PathVariable String trackingNumber,
        @RequestParam Instant deliveredAt
    ) {
        var update = new Deliver(deliveredAt);
        commands.updateShipmentStatus(trackingNumber, update);
        return new OrderShipmentStatusUpdateResponse(trackingNumber, update);
    }

    @GetMapping
    OrderShipmentProjection findOrderShipment(
        @RequestParam(required = false) String orderId,
        @RequestParam(required = false) String trackingNumber
    ) {
        if(orderId != null && trackingNumber == null)
            return queries.findByOrderId(orderId, OrderShipmentProjection.class)
                .orElseThrow();

        if(trackingNumber != null && orderId == null)
            return queries.findByTrackingNumber(trackingNumber, OrderShipmentProjection.class)
                .orElseThrow();

        throw new IllegalArgumentException("either one of orderId and trackingNumber must be provided");
    }

    class OrderShipmentStatusUpdateResponse extends RepresentationModel<OrderShipmentProjection> {
        OrderShipmentStatusUpdateResponse(String trackingNumber, OrderShipmentCommands update) {
            add(linkSelf(trackingNumber));

            switch (update) {
                case Pack __ -> add(linkShip(trackingNumber));
                case Ship __ -> add(linkDeliver(trackingNumber));
                case Deliver __ -> {
                    // No further actions after delivery
                }
            }
        }
    }

}