package io.github.etr.playground.domain.shipping;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import io.github.etr.playground.domain.shipping.ShippingUpdate.Delivery;
import io.github.etr.playground.domain.shipping.ShippingUpdate.Packing;
import io.github.etr.playground.domain.shipping.ShippingUpdate.Shipping;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderShipping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderId;
    private String username;
    private String carrier;

    @Column(unique = true)
    private String trackingNumber = UUID.randomUUID().toString();
    private ShippingStatus status = ShippingStatus.NEW;
    private Instant updatedAt = Instant.now();

    @Embedded
    private Packing packing;
    @Embedded
    private Shipping shipping;
    @Embedded
    private Delivery delivery;

    public void update(ShippingUpdate update) {
        this.updatedAt = Instant.now();
        this.status = ShippingStatus.afterShippingUpdate(update);

        switch (update) {
            case Delivery it -> this.delivery = it;
            case Packing it -> this.packing = it;
            case Shipping it -> this.shipping = it;
        }
    }

    public OrderShipping(String orderId, String username, String carrier) {
        this.orderId = orderId;
        this.username = username;
        this.carrier = carrier;
    }
}