package io.github.etr.playground.shipping.domain;

import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.awaitility.Awaitility.await;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.etr.playground.IntegrationTest;

@DisplayName("Given 'stock-reserved' event received on Kafka")
class OrderShipmentsCommandHandlerTest extends IntegrationTest {

    @Autowired
    private OrderShipmentsRepository shipmentsRepo;

    private static String trackingNumber;

    @Test
    @DisplayName("Then a new OrderShipment is saved to DB")
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

    @Nested
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    @DisplayName("When PUT /shipments/:id/pack is received over HTTP")
    class PackOrder {

        @Test
        @Order(1)
        @DisplayName("Then 'order-packed' is published to Kafka")
        void shouldPublishOrderPacked() {
            assumeThat(trackingNumber).isNotNull();

            sendPutRequest("/shipments/%s/pack".formatted(trackingNumber),
                Map.of("packedAt", "2025-09-22T11:37:24.000Z", "estimatedShippingDate", "2025-09-22T11:37:24.000Z"));

            await().untilAsserted((() -> then(outgoingKafkaMessages.messagesFor("order-packed"))
                .hasSize(1).first().asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                    "orderId", "order-100",
                    "username", "john_doe",
                    "trackingNumber", trackingNumber
                ))));
        }

        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
        @DisplayName("When PUT /shipments/:id/ship is received over HTTP")
        class ShipOrder {

            @Test
            @Order(1)
            @DisplayName("Then 'order-shipped' is published to kafka")
            void shouldPublishOrderShipped() {
                assumeThat(trackingNumber).isNotNull();

                sendPutRequest("/shipments/%s/ship".formatted(trackingNumber),
                    Map.of("shippedAt", "2025-09-22T11:47:24.000Z", "estimatedDeliveryDate", "2025-09-22T11:47:24.000Z"));

                await().untilAsserted((() -> then(outgoingKafkaMessages.messagesFor("order-shipped"))
                    .hasSize(1).first().asInstanceOf(MAP)
                    .containsAllEntriesOf(Map.of(
                        "orderId", "order-100",
                        "username", "john_doe",
                        "trackingNumber", trackingNumber
                    ))));
            }

            @Nested
            @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
            @DisplayName("When PUT /shipments/:id/deliver is received over HTTP")
            class DeliverOrder {

                @Test
                @Order(1)
                @DisplayName("Then 'order-delivered' is published to kafka")
                void shouldPublishOrderDelivered() {
                    assumeThat(trackingNumber).isNotNull();

                    sendPutRequest("/shipments/%s/deliver".formatted(trackingNumber), Map.of("deliveredAt", "2025-09-22T11:47:24.000Z"));

                    await().untilAsserted((() -> then(outgoingKafkaMessages.messagesFor("order-delivered"))
                        .hasSize(1).first().asInstanceOf(MAP)
                        .containsAllEntriesOf(Map.of(
                            "orderId", "order-100",
                            "username", "john_doe",
                            "trackingNumber", trackingNumber
                        ))));
                }
            }
        }
    }

}