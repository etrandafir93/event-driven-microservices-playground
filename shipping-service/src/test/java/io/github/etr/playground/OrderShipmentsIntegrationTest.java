package io.github.etr.playground;

import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.etr.playground.shipping.domain.OrderShipmentsRepository;

@DisplayName("Given 'stock-reserved' event received on Kafka")
class OrderShipmentsIntegrationTest extends IntegrationTest {

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

        await().untilAsserted(() ->
            assertDoesNotThrow(() ->
                sendGetRequest("/shipments?orderId=order-100")));

        var shipment = sendGetRequest("/shipments?orderId=order-100");
        then(shipment)
            .containsEntry("orderId", "order-100")
            .containsEntry("username", "john_doe")
            .containsKey("trackingNumber")
            .containsEntry("packedAt", null)
            .containsEntry("estimatedShipping", null)
            .containsEntry("shippedAt", null)
            .containsEntry("estimatedDelivery", null)
            .containsEntry("deliveredAt", null);

        trackingNumber = shipment.get("trackingNumber").toString();
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
                Map.of("packedAt", "2025-09-22T12:00:00Z", "estimatedShippingDate", "2025-09-23T12:00:00Z"));

            await().untilAsserted((() -> then(outgoingKafkaMessages.messagesFor("order-packed"))
                .hasSize(1).first().asInstanceOf(MAP)
                .containsAllEntriesOf(Map.of(
                    "orderId", "order-100",
                    "username", "john_doe",
                    "trackingNumber", trackingNumber
                ))));
        }

        @Test
        @Order(2)
        @DisplayName("And GET /shipments?trackingNumber=:id shows order as 'packed'")
        void shouldShowOrderAsPacked() {
            assumeThat(trackingNumber).isNotNull();

            var shipment = sendGetRequest("/shipments?trackingNumber=" + trackingNumber);
            then(shipment)
                .containsEntry("packedAt", "2025-09-22T12:00:00Z")
                .containsEntry("estimatedShipping", "2025-09-23T12:00:00Z")
                .containsEntry("shippedAt", null)
                .containsEntry("estimatedDelivery", null)
                .containsEntry("deliveredAt", null);
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
                    Map.of("shippedAt", "2025-09-23T12:00:00Z", "estimatedDeliveryDate", "2025-09-24T12:00:00Z"));

                await().untilAsserted((() -> then(outgoingKafkaMessages.messagesFor("order-shipped"))
                    .hasSize(1).first().asInstanceOf(MAP)
                    .containsAllEntriesOf(Map.of(
                        "orderId", "order-100",
                        "username", "john_doe",
                        "trackingNumber", trackingNumber
                    ))));
            }

            @Test
            @Order(2)
            @DisplayName("And GET /shipments?trackingId=:id shows order as 'shipped'")
            void shouldShowOrderAsPacked() {
                assumeThat(trackingNumber).isNotNull();

                var shipment = sendGetRequest("/shipments?trackingNumber=" + trackingNumber);
                then(shipment)
                    .containsEntry("packedAt", "2025-09-22T12:00:00Z")
                    .containsEntry("estimatedShipping", "2025-09-23T12:00:00Z")
                    .containsEntry("shippedAt", "2025-09-23T12:00:00Z")
                    .containsEntry("estimatedDelivery", "2025-09-24T12:00:00Z")
                    .containsEntry("deliveredAt", null);
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

                    sendPutRequest("/shipments/%s/deliver".formatted(trackingNumber), Map.of(
                        "deliveredAt", "2025-09-24T12:00:00Z"));

                    await().untilAsserted((() -> then(outgoingKafkaMessages.messagesFor("order-delivered"))
                        .hasSize(1).first().asInstanceOf(MAP)
                        .containsAllEntriesOf(Map.of(
                            "orderId", "order-100",
                            "username", "john_doe",
                            "trackingNumber", trackingNumber
                        ))));
                }

                @Test
                @Order(2)
                @DisplayName("And GET /shipments?trackingId=:id shows order as 'delivered'")
                void shouldShowOrderAsPacked() {
                    assumeThat(trackingNumber).isNotNull();

                    var shipment = sendGetRequest("/shipments?trackingNumber=" + trackingNumber);
                    then(shipment)
                        .containsEntry("packedAt", "2025-09-22T12:00:00Z")
                        .containsEntry("estimatedShipping", "2025-09-23T12:00:00Z")
                        .containsEntry("shippedAt", "2025-09-23T12:00:00Z")
                        .containsEntry("estimatedDelivery", "2025-09-24T12:00:00Z")
                        .containsEntry("deliveredAt", "2025-09-24T12:00:00Z");
                }
            }
        }
    }

}