package io.github.etr.playground.shipping.domain;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrderShipmentsRepository extends JpaRepository<OrderShipment, Long>, OrderShipmentQueries {

}