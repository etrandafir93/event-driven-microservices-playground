package io.github.etr.playground.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.github.etr.playground.IntegrationTest;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrderStatusTransitionsTest extends IntegrationTest {

    private static String orderId;

    @Test
    @Order(1)
    @DisplayName("new -> pending")
    void shouldCreateOrder_inPendingStatus() {
        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": { "TV-55-SAM-QLED": 1 }
            }
            """);

        assertThat(httpResponse)
            .containsKey("orderId")
            .containsEntry("status", "PENDING");

        this.orderId = httpResponse.get("orderId").toString();
    }

    @Test
    @Order(2)
    @DisplayName("pending -> shipped")
    void shouldUpdateOrderStatus_fromPendingToShipped() {
        assumeThat(orderId).isNotNull();

        sendKafkaMessage("order-shipped", orderId, """
            {
                "orderId": "%s",
                "username": "john_doe",
                "trackingNumber": "1234567890",
                "carrier": "FedEx",
                "shippedAt": "2024-08-17T14:30:00Z",
                "estimatedDelivery": "2024-08-20T18:00:00Z"
            }
            """.formatted(orderId));

        await().untilAsserted(() -> {
            var resp = sendGetRequest("/v1/orders/" + orderId);
            then(resp).containsEntry("status", "SHIPPED");
        });
    }

    @Test
    @Order(3)
    @DisplayName("shipped -> delivered")
    void shouldUpdateOrderStatus_fromShippedToDelivered() {
        assumeThat(orderId).isNotNull();

        sendKafkaMessage("order-delivered", orderId, """
            {
                "orderId": "%s",
                "username": "john_doe",
                "trackingNumber": "1234567890",
                "carrier": "FedEx",
                "deliveredAt": "2024-08-17T14:30:00Z"
            }
            """.formatted(orderId));

        await().untilAsserted(() -> {
            var resp = sendGetRequest("/v1/orders/" + orderId);
            then(resp).containsEntry("status", "DELIVERED");
        });
    }

}
