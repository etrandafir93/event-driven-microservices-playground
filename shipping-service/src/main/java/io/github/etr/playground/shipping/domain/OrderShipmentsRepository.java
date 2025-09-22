package io.github.etr.playground.shipping.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderShipmentsRepository extends JpaRepository<OrderShipment, Long> {

    <T> Optional<T> findByTrackingNumber(String trackingNumber, Class<T> type);

    <T> Optional<T> findByOrderId(String findByOrderId, Class<T> type);

}