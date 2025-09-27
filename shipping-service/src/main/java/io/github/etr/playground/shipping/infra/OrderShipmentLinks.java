package io.github.etr.playground.shipping.infra;

import static java.time.Instant.now;
import static java.time.temporal.ChronoUnit.DAYS;

import org.springframework.hateoas.Link;

import lombok.experimental.UtilityClass;

@UtilityClass
class OrderShipmentLinks {

    public static Link linkSelf(String trackingNumber) {
        return Link.of("/api/v1/shipments?trackingNumber=%s".formatted(trackingNumber))
            .withType("GET")
            .withSelfRel();
    }

    public static Link linkPack(String trackingNumber) {
        return Link.of("/api/v1/shipments/%s/pack".formatted(trackingNumber)
                + "?packedAt=%s".formatted(now())
                + "&estimatedShippingDate=%s".formatted(now().plus(1, DAYS))
            )
            .withType("PUT")
            .withRel("pack");
    }

    public static Link linkShip(String trackingNumber) {
        return Link.of("/api/v1/shipments/%s/ship".formatted(trackingNumber)
                + "?shippedAt=%s".formatted(now())
                + "&estimatedDeliveryDate=%s".formatted(now().plus(1, DAYS)))
            .withType("PUT")
            .withRel("ship");
    }

    public static Link linkDeliver(String trackingNumber) {
        return Link.of("/api/v1/shipments/%s/deliver".formatted(trackingNumber)
                + "?deliveredAt=%s".formatted(now()))
            .withType("PUT")
            .withRel("deliver");
    }
}
