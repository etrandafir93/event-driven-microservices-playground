package io.github.etr.playground.shipping.domain;

import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import java.util.Map;

import org.assertj.core.api.Assumptions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.etr.playground.IntegrationTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderShipmentsCommandHandlerTest extends IntegrationTest {

    @Autowired
    private OrderShipmentsRepository shipmentsRepo;

    @Autowired
    private OrderShipmentsCommandHandler commandHandler;

    @Autowired
    private OrderShipmentsQueries orderShipmentsQueries;

    private static String trackingNumber;

    @Test
    @Order(1)
    void shouldCreateShipmentWhenStockReserved() {
        sendKafkaMessage("stock-reserved", "order-100", """
            {
                "orderId": "order-100",
                "username": "john_doe",
                "itemSku": "DUMMY-SKU-10"
            }
            """);

        await().until(() -> shipmentsRepo.findByOrderId("order-100")
            .isPresent());

        OrderShipment shipment = shipmentsRepo.findByOrderId("order-100")
            .orElseThrow();

        then(shipment)
            .hasFieldOrPropertyWithValue("orderId", "order-100")
            .hasFieldOrPropertyWithValue("username", "john_doe")
            .hasFieldOrProperty("trackingNumber");

        trackingNumber = shipment.trackingNumber();
    }

    @Test
    @Order(2)
    void shouldPublishOrderPacked() {
        assumeThat(trackingNumber).isNotNull();

        sendPutRequest("/shipments/%s/pack".formatted(trackingNumber), Map.of(
            "packedAt", "2025-09-22T11:37:24.000Z",
            "estimatedShippingDate", "2025-09-22T11:37:24.000Z"
        ));

        await().untilAsserted((() ->
            then(outgoingKafkaMessages.messagesFor("order-packed"))
                .hasSize(1).first()
                .hasFieldOrPropertyWithValue("orderId", "order-100")
                .hasFieldOrPropertyWithValue("username", "john_doe")
                .hasFieldOrPropertyWithValue("trackingNumber", trackingNumber)));
    }

    @Test
    @Order(3)
    void shouldPublishOrderShipped() {
        assumeThat(trackingNumber).isNotNull();

        sendPutRequest("/shipments/%s/ship".formatted(trackingNumber), Map.of(
            "shippedAt", "2025-09-22T11:47:24.000Z",
            "estimatedDeliveryDate", "2025-09-22T11:47:24.000Z"
        ));

        await().untilAsserted((() ->
            then(outgoingKafkaMessages.messagesFor("order-shipped"))
                .hasSize(1).first()
                .hasFieldOrPropertyWithValue("orderId", "order-100")
                .hasFieldOrPropertyWithValue("username", "john_doe")
                .hasFieldOrPropertyWithValue("trackingNumber", trackingNumber)));
    }


    @Test
    @Order(4)
    void shouldPublishOrderDelivered() {
        assumeThat(trackingNumber).isNotNull();

        sendPutRequest("/shipments/%s/deliver".formatted(trackingNumber), Map.of(
            "deliveredAt", "2025-09-22T11:47:24.000Z"
        ));

        await().untilAsserted((() ->
            then(outgoingKafkaMessages.messagesFor("order-delivered"))
                .hasSize(1).first()
                .hasFieldOrPropertyWithValue("orderId", "order-100")
                .hasFieldOrPropertyWithValue("username", "john_doe")
                .hasFieldOrPropertyWithValue("trackingNumber", trackingNumber)));
    }
}