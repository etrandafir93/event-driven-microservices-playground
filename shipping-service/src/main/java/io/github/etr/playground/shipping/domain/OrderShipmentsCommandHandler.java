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

    public void updateStatus(String trackingId, ShippingUpdate update) {
        var shipment = shipmentsRepo.findByTrackingId(trackingId)
            .orElseThrow();

        shipment.update(update);
        shipment = shipmentsRepo.save(shipment);

        publishDomainEvent(shipment, update);
    }

    private void publishDomainEvent(OrderShipment shipment, ShippingUpdate update) {
        Object domainEvet = switch (update) {
            case Delivery it ->
                new OrderDelivered(shipment.getOrderId(), shipment.getUsername(), shipment.getTrackingNumber(), shipment.getCarrier(), it.shippedAt(),
                    it.deliveredAt());
            case Packing it ->
                new OrderPacked(shipment.getOrderId(), shipment.getUsername(), shipment.getTrackingNumber(), shipment.getCarrier(), it.estimatedShipping(),
                    it.estimatedDelivery());
            case Shipping it ->
                new OrderShipped(shipment.getOrderId(), shipment.getUsername(), shipment.getTrackingNumber(), shipment.getCarrier(), it.shippedAt(),
                    it.estimatedDelivery());
        };
        applicationEvents.publishEvent(domainEvet);
    }

}
