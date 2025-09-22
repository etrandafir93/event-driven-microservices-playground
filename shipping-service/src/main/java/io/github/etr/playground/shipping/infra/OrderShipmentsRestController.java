package io.github.etr.playground.shipping.infra;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.etr.playground.shipping.domain.OrderShipmentsCommandHandler;
import io.github.etr.playground.shipping.domain.OrderShipmentsQueries;
import io.github.etr.playground.shipping.domain.ShippingUpdate;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/shipments")
public class OrderShipmentsRestController {

    private final OrderShipmentsCommandHandler commandHandler;
    private final OrderShipmentsQueries queries;

    @PutMapping("/{trackingId}/pack")
    void orderPacked(
        @PathVariable String trackingId,
        @RequestParam Instant packedAt,
        @RequestParam Instant estimatedShippingDate
    ) {
        var update = new ShippingUpdate.Packing(packedAt, estimatedShippingDate);
        commandHandler.updateShipmentStatus(trackingId, update);
    }

    @PutMapping("/{trackingId}/ship")
    void orderShipped(
        @PathVariable String trackingId,
        @RequestParam Instant shippedAt,
        @RequestParam Instant estimatedDeliveryDate
    ) {
        var update = new ShippingUpdate.Shipping(shippedAt, estimatedDeliveryDate);
        commandHandler.updateShipmentStatus(trackingId, update);
    }

    @PutMapping("/{trackingId}/deliver")
    void orderDelivered(
        @PathVariable String trackingId,
        @RequestParam Instant deliveredAt
    ) {
        var update = new ShippingUpdate.Delivery(deliveredAt);
        commandHandler.updateShipmentStatus(trackingId, update);
    }

    @GetMapping
    OrderShipmentProjection findOrderShipment(
        @RequestParam(required = false) String orderId,
        @RequestParam(required = false) String trackingNumber
    ) {
        if(orderId != null && trackingNumber == null)
            return queries.byOrderId(orderId)
                .orElseThrow();

        if(trackingNumber != null && orderId == null)
            return queries.byTrackingNumber(trackingNumber)
                .orElseThrow();

        throw new IllegalArgumentException("either one of orderId and trackingNumber must be provided");
    }


}