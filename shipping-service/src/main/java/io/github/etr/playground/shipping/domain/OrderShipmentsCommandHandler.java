package io.github.etr.playground.shipping.domain;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import io.github.etr.playground.shipping.domain.ShippingUpdate.Delivery;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Packing;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Shipping;
import io.github.etr.playground.shipping.events.OrderDelivered;
import io.github.etr.playground.shipping.events.OrderPacked;
import io.github.etr.playground.shipping.events.OrderShipped;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderShipmentsCommandHandler {

    private final OrderShipmentsRepository shipmentsRepo;
    private final ApplicationEventPublisher applicationEvents;

    public void createShipment(String orderId, String username) {
        shipmentsRepo.save(new OrderShipment(orderId, username, "FedEx"));
    }

    public void updateShipmentStatus(String trackingId, ShippingUpdate update) {
        var shipment = shipmentsRepo.findByTrackingNumber(trackingId)
            .orElseThrow();

        shipment.update(update);
        shipment = shipmentsRepo.save(shipment);

        publishDomainEvent(shipment, update);
    }

    private void publishDomainEvent(OrderShipment shipment, ShippingUpdate update) {
        Object domainEvet = switch (update) {
            case Delivery __ ->
                new OrderDelivered(shipment.orderId(), shipment.username(), shipment.trackingNumber(), shipment.carrier(), shipment.shippedAt(),
                    shipment.deliveredAt());
            case Packing __ ->
                new OrderPacked(shipment.orderId(), shipment.username(), shipment.trackingNumber(), shipment.carrier(), shipment.estimatedShipping(),
                    shipment.estimatedDelivery());
            case Shipping __ ->
                new OrderShipped(shipment.orderId(), shipment.username(), shipment.trackingNumber(), shipment.carrier(), shipment.shippedAt(),
                    shipment.estimatedDelivery());
        };
        applicationEvents.publishEvent(domainEvet);
    }

}
