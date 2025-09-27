package io.github.etr.playground.shipping.domain;

import static io.github.etr.playground.shipping.domain.ShipmentStatus.DELIVERED;
import static io.github.etr.playground.shipping.domain.ShipmentStatus.NEW;
import static io.github.etr.playground.shipping.domain.ShipmentStatus.PACKED;
import static io.github.etr.playground.shipping.domain.ShipmentStatus.SHIPPED;
import static org.springframework.util.Assert.state;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Deliver;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Pack;
import io.github.etr.playground.shipping.domain.OrderShipmentCommands.Ship;
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

    private Instant packedAt;
    private Instant estimatedShipping;
    private Instant shippedAt;
    private Instant estimatedDelivery;
    private Instant deliveredAt;

    public void update(OrderShipmentCommands update) {
        validateStatus(update);

        this.updatedAt = Instant.now();
        this.status = ShipmentStatus.afterShippingUpdate(update);

        switch (update) {
            case Pack it -> {
                this.packedAt = it.packedAt();
                this.estimatedShipping = it.estimatedShipping();
            }
            case Ship it -> {
                this.shippedAt = it.shippedAt();
                this.estimatedDelivery = it.estimatedDelivery();
            }
            case Deliver it ->
                this.deliveredAt = it.deliveredAt();
        }
    }

    private void validateStatus(OrderShipmentCommands update) {
        switch (update) {
            case Pack __ -> state(this.status == NEW, invalidStatusTransitionMsg(orderId, status, PACKED));
            case Ship __ -> state(this.status == PACKED, invalidStatusTransitionMsg(orderId, status, SHIPPED));
            case Deliver __ -> state(this.status == SHIPPED, invalidStatusTransitionMsg(orderId, status, DELIVERED));
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