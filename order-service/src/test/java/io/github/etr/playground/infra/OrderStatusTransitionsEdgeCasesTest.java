package io.github.etr.playground.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.github.etr.playground.IntegrationTest;
import lombok.SneakyThrows;

class OrderStatusTransitionsEdgeCasesTest extends IntegrationTest {

    @Test
    @SneakyThrows
    void shouldHandleMessagesOutOfOrder() {
        String orderId = givenNewOrder();
        givenOrderDelivered(orderId, "idempotency-key-1");

        Thread.sleep(1_000);
        givenOrderShipped(orderId, "idempotency-key-2");

        thenOrderShouldHaveStatus(orderId, "DELIVERED");
    }

    @Test
    @SneakyThrows
    void shouldHandleDuplicates() {
        String orderId = givenNewOrder();

        givenOrderShipped(orderId, "idempotency-key-3");
        givenOrderShipped(orderId, "idempotency-key-3");
        givenOrderShipped(orderId, "idempotency-key-3");
        Thread.sleep(200);

        givenOrderDelivered(orderId, "idempotency-key-4");
        givenOrderDelivered(orderId, "idempotency-key-4");
        givenOrderDelivered(orderId, "idempotency-key-4");
        Thread.sleep(200);

        thenOrderShouldHaveStatus(orderId, "DELIVERED");
    }

    @Test
    @SneakyThrows
    void shouldHandlePoisonPillMessages() {
        String orderId = givenNewOrder();
        givenOrderShipped(orderId, "idempotency-key-5");

        sendKafkaMessage("order-delivered", orderId,
            " === NOT A VALID JSON! === ");

        givenOrderDelivered(orderId, "idempotency-key-6");

        thenOrderShouldHaveStatus(orderId, "DELIVERED");
    }

    private void thenOrderShouldHaveStatus(String orderId, String expectedSts) {
        await().untilAsserted(() -> {
            var resp = sendGetRequest("/v1/orders/" + orderId);
            then(resp).containsEntry("status", expectedSts);
        });
    }

    private void givenOrderShipped(String orderId, String idempotencyKey) {
        sendKafkaMessage("order-shipped", orderId, """
            {
                "orderId": "%s",
                "username": "john_doe",
                "trackingNumber": "1234567890",
                "carrier": "FedEx",
                "shippedAt": "2024-08-17T14:30:00Z",
                "estimatedDelivery": "2024-08-20T18:00:00Z"
            }
            """.formatted(orderId), idempotencyKey);
    }

    private void givenOrderDelivered(String orderId) {
        givenOrderDelivered(orderId, UUID.randomUUID().toString());
    }

    private void givenOrderDelivered(String orderId, String idempotencyKey) {
        sendKafkaMessage("order-delivered", orderId, """
            {
                "orderId": "%s",
                "username": "john_doe",
                "trackingNumber": "1234567890",
                "carrier": "FedEx",
                "deliveredAt": "2024-08-17T14:30:00Z"
            }
            """.formatted(orderId), idempotencyKey);
    }

    private String givenNewOrder() {
        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": { "TV-55-SAM-QLED": 1 }
            }
            """);

        assertThat(httpResponse)
            .containsKey("orderId")
            .containsEntry("status", "PENDING");

        return httpResponse.get("orderId").toString();
    }


}
