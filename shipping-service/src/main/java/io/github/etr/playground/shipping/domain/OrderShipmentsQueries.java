package io.github.etr.playground.shipping.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderShipmentsQueries {

    private final OrderShipmentsRepository shipmentsRepo;

    public List<OrderShipment> byUsername(String username) {
        return shipmentsRepo.findByUsername(username);
    }

    public Optional<OrderShipment> byTrackingNumber(String trackingNumber) {
        return shipmentsRepo.findByTrackingNumber(trackingNumber);
    }

    public Optional<OrderShipment> byOrderId(String orderId) {
        return shipmentsRepo.findByOrderId(orderId);
    }

}
