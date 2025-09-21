package io.github.etr.playground.shipping.infra;

import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.github.etr.playground.shipping.domain.ShippingUpdate;
import io.github.etr.playground.shipping.domain.OrderShipmentsCommandHandler;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/shipments")
class OrderShipmentsRestController {

    private final OrderShipmentsCommandHandler commandHandler;

    @GetMapping("/isAlive")
    public String isAlive() {
        return "OK";
    }

    @PutMapping("/{trackingId}/pack")
    void orderPacked(
        @PathVariable String trackingId,
        @RequestParam Instant estimatedShippingDate
    ) {
        var update = new ShippingUpdate.Packing(estimatedShippingDate);
        commandHandler.updateStatus(trackingId, update);
    }

    @PutMapping("/{trackingId}/ship")
    void orderShipped(
        @PathVariable String trackingId,
        @RequestParam Instant shippedAt,
        @RequestParam Instant estimatedDeliveryDate
    ) {
        var update = new ShippingUpdate.Shipping(shippedAt, estimatedDeliveryDate);
        commandHandler.updateStatus(trackingId, update);
    }

    @PutMapping("/{trackingId}/deliver")
    void orderDelivered(
        @PathVariable String trackingId,
        @RequestParam Instant deliveredAt
    ) {
        var update = new ShippingUpdate.Delivery(deliveredAt);
        commandHandler.updateStatus(trackingId, update);
    }

}