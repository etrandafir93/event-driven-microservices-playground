package io.github.etr.playground.shipping.domain;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.etr.playground.shipping.domain.ShippingUpdate.Delivery;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Packing;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Shipping;
import io.github.etr.playground.shipping.domain.events.OrderDelivered;
import io.github.etr.playground.shipping.domain.events.OrderPacked;
import io.github.etr.playground.shipping.domain.events.OrderShipped;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderShipmentsCommandHandler {

    private final OrderShipmentsRepository shipmentsRepo;
    private final ApplicationEventPublisher applicationEvents;

    public String createShipment(String orderId, String username) {
        var shipment = shipmentsRepo.save(new OrderShipment(orderId, username, "FedEx"));
        return shipment.trackingNumber();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        // sae dummy events // fixme
        System.err.println(createShipment("order-1", "user-1"));
        System.err.println(createShipment("order-2", "user-2"));
        System.err.println(createShipment("order-3", "user-3"));
    }

    @Transactional
    public void updateShipmentStatus(String trackingId, ShippingUpdate update) {
        var shipment = shipmentsRepo.findByTrackingNumber(trackingId, OrderShipment.class)
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
