package io.github.etr.playground.shipping.domain;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderShipmentsRepository extends JpaRepository<OrderShipment, Long> {

    Optional<OrderShipment> findByTrackingId(String trackingId);
}