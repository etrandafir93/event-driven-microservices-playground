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
import io.github.etr.playground.shipping.domain.OrderShipmentsRepository;
import io.github.etr.playground.shipping.domain.ShippingUpdate;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Delivery;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Packing;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Shipping;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/shipments")
class OrderShipmentsRestController {

    private final OrderShipmentsCommandHandler commands;
    private final OrderShipmentsRepository queries;

    @PutMapping("/{trackingNumber}/pack")
    OrderShipmentStatusUpdateResponse orderPacked(
        @PathVariable String trackingNumber,
        @RequestParam Instant packedAt,
        @RequestParam Instant estimatedShippingDate
    ) {
        var update = new Packing(packedAt, estimatedShippingDate);
        commands.updateShipmentStatus(trackingNumber, update);
        return new OrderShipmentStatusUpdateResponse(trackingNumber, update);
    }

    @PutMapping("/{trackingNumber}/ship")
    OrderShipmentStatusUpdateResponse orderShipped(
        @PathVariable String trackingNumber,
        @RequestParam Instant shippedAt,
        @RequestParam Instant estimatedDeliveryDate
    ) {
        var update = new Shipping(shippedAt, estimatedDeliveryDate);
        commands.updateShipmentStatus(trackingNumber, update);
        return new OrderShipmentStatusUpdateResponse(trackingNumber, update);
    }

    @PutMapping("/{trackingNumber}/deliver")
    OrderShipmentStatusUpdateResponse orderDelivered(
        @PathVariable String trackingNumber,
        @RequestParam Instant deliveredAt
    ) {
        var update = new Delivery(deliveredAt);
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
        OrderShipmentStatusUpdateResponse(String trackingNumber, ShippingUpdate update) {
            add(linkSelf(trackingNumber));

            switch (update) {
                case Packing __ -> add(linkShip(trackingNumber));
                case Shipping __ -> add(linkDeliver(trackingNumber));
                case Delivery __ -> {
                    // No further actions after delivery
                }
            }
        }
    }

}