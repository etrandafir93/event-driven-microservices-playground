package io.github.etr.playground.shipping.infra;

import java.time.Instant;

import org.springframework.hateoas.Link;
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
        this.estimatedShipping = estimatedDelivery;
        this.deliveredAt = deliveredAt;
        addSelfAndActionLinks();
    }

    private void addSelfAndActionLinks() {
        add(Link.of("/api/v1/shipments?trackingNumber=" + trackingNumber)
            .withSelfRel());
        add(Link.of("/api/v1/shipments?orderId=" + orderId)
            .withSelfRel());

        if (packedAt == null)
            add(Link.of("/api/v1/shipments/" + trackingNumber + "/pack")
                .withRel("pack"));
        else if (shippedAt == null)
            add(Link.of("/api/v1/shipments/" + trackingNumber + "/ship")
                .withRel("ship"));
        else if (deliveredAt == null)
            add(Link.of("/api/v1/shipments/" + trackingNumber + "/deliver")
                .withRel("deliver"));
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
}
