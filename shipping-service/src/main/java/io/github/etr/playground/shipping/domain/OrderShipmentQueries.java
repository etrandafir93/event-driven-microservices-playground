package io.github.etr.playground.shipping.domain;

import java.util.Optional;

public interface OrderShipmentQueries {

    <T> Optional<T> findByTrackingNumber(String trackingNumber, Class<T> type);

    <T> Optional<T> findByOrderId(String findByOrderId, Class<T> type);

}