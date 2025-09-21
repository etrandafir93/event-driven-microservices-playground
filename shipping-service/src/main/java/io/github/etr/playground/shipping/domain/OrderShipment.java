package io.github.etr.playground.shipping.domain;

import static io.github.etr.playground.shipping.domain.ShipmentStatus.DELIVERED;
import static io.github.etr.playground.shipping.domain.ShipmentStatus.NEW;
import static io.github.etr.playground.shipping.domain.ShipmentStatus.PACKED;
import static io.github.etr.playground.shipping.domain.ShipmentStatus.SHIPPED;
import static org.springframework.util.Assert.state;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import io.github.etr.playground.shipping.domain.ShippingUpdate.Delivery;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Packing;
import io.github.etr.playground.shipping.domain.ShippingUpdate.Shipping;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class OrderShipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String username;
    private String carrier;

    @Column(unique = true)
    private String trackingNumber = UUID.randomUUID().toString();
    private ShipmentStatus status = NEW;
    private Instant updatedAt = Instant.now();

    private Instant estimatedShipping;
    private Instant shippedAt;

    private Instant estimatedDelivery;
    private Instant deliveredAt;

    public void update(ShippingUpdate update) {
        validateStatus(update);

        this.updatedAt = Instant.now();
        this.status = ShipmentStatus.afterShippingUpdate(update);

        switch (update) {
            case Packing it ->
                this.estimatedShipping = it.estimatedShipping();
            case Shipping it -> {
                this.shippedAt = it.shippedAt();
                this.estimatedDelivery = it.estimatedDelivery();
            }
            case Delivery it ->
                this.deliveredAt = it.deliveredAt();
        }
    }

    private void validateStatus(ShippingUpdate update) {
        switch (update) {
            case Delivery __ -> state(this.status == SHIPPED, invalidStatusTransitionMsg(orderId, status, DELIVERED));
            case Packing __ -> state(this.status == NEW, invalidStatusTransitionMsg(orderId, status, PACKED));
            case Shipping __ -> state(this.status == PACKED, invalidStatusTransitionMsg(orderId, status, SHIPPED));
        }
    }

    private static String invalidStatusTransitionMsg(String orderId, ShipmentStatus currentStatus, ShipmentStatus newStatus) {
        return "the order %s has status='%s' and cannot transition directly to status='%s'!"
            .formatted(orderId, currentStatus, newStatus);
    }

    public OrderShipment(String orderId, String username, String carrier) {
        this.orderId = orderId;
        this.username = username;
        this.carrier = carrier;
    }
}