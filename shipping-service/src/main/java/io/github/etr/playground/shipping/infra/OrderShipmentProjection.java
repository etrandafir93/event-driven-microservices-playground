package io.github.etr.playground.shipping.infra;

import static io.github.etr.playground.shipping.infra.HateoasLinks.linkDeliver;
import static io.github.etr.playground.shipping.infra.HateoasLinks.linkPack;
import static io.github.etr.playground.shipping.infra.HateoasLinks.linkSelf;
import static io.github.etr.playground.shipping.infra.HateoasLinks.linkShip;

import java.time.Instant;

import org.springframework.hateoas.RepresentationModel;

class OrderShipmentProjection extends RepresentationModel<OrderShipmentProjection> {
    String trackingNumber;
    String username;
    String orderId;
    String carrier;
    Instant packedAt;
    Instant estimatedShipping;
    Instant shippedAt;
    Instant estimatedDelivery;
    Instant deliveredAt;

    public OrderShipmentProjection(String trackingNumber, String username, String orderId, String carrier, Instant packedAt, Instant estimatedShipping,
        Instant shippedAt, Instant estimatedDelivery, Instant deliveredAt) {
        this.trackingNumber = trackingNumber;
        this.username = username;
        this.orderId = orderId;
        this.carrier = carrier;
        this.packedAt = packedAt;
        this.estimatedShipping = estimatedShipping;
        this.shippedAt = shippedAt;
        this.estimatedDelivery = estimatedDelivery;
        this.deliveredAt = deliveredAt;
        addSelfAndActionLinks();
    }

    private void addSelfAndActionLinks() {
        add(linkSelf(trackingNumber));

        if (packedAt == null)
            add(linkPack(trackingNumber));

        else if (shippedAt == null)
            add(linkShip(trackingNumber));

         else if (deliveredAt == null)
            add(linkDeliver(trackingNumber));
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }
    public String getUsername() {
        return username;
    }
    public String getOrderId() {
        return orderId;
    }
    public String getCarrier() {
        return carrier;
    }
    public Instant getEstimatedShipping() {
        return estimatedShipping;
    }
    public Instant getShippedAt() {
        return shippedAt;
    }
    public Instant getEstimatedDelivery() {
        return estimatedDelivery;
    }
    public Instant getDeliveredAt() {
        return deliveredAt;
    }
    public Instant getPackedAt() {
        return packedAt;
    }
}
