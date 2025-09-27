package io.github.etr.playground.shipping.domain;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Deliver;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Pack;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Ship;
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
    public void updateShipmentStatus(String trackingId, OrderShipmentCommands update) {
        var shipment = shipmentsRepo.findByTrackingNumber(trackingId, OrderShipment.class)
            .orElseThrow();

        shipment.update(update);
        shipment = shipmentsRepo.save(shipment);

        publishDomainEvent(shipment, update);
    }

    private void publishDomainEvent(OrderShipment shipment, OrderShipmentCommands update) {
        Object domainEvet = switch (update) {
            case Deliver __ ->
                new OrderDelivered(shipment.orderId(), shipment.username(), shipment.trackingNumber(), shipment.carrier(), shipment.shippedAt(),
                    shipment.deliveredAt());
            case Pack __ ->
                new OrderPacked(shipment.orderId(), shipment.username(), shipment.trackingNumber(), shipment.carrier(), shipment.estimatedShipping(),
                    shipment.estimatedDelivery());
            case Ship __ ->
                new OrderShipped(shipment.orderId(), shipment.username(), shipment.trackingNumber(), shipment.carrier(), shipment.shippedAt(),
                    shipment.estimatedDelivery());
        };
        applicationEvents.publishEvent(domainEvet);
    }

}
