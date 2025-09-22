package io.github.etr.playground.shipping.domain;

import java.util.Optional;

import org.springframework.stereotype.Service;

import io.github.etr.playground.shipping.infra.OrderShipmentProjection;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderShipmentsQueries {

    private final OrderShipmentsRepository shipmentsRepo;

    public Optional<OrderShipmentProjection> byTrackingNumber(String trackingNumber) {
        return shipmentsRepo.findByTrackingNumber(trackingNumber, OrderShipmentProjection.class);
    }

    public Optional<OrderShipmentProjection> byOrderId(String orderId) {
        return shipmentsRepo.findByOrderId(orderId, OrderShipmentProjection.class);
    }
}
