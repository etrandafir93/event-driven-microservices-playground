package io.github.etr.playground.infra;

import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.Test;

import io.github.etr.playground.IntegrationTest;

class OrderShippedTest extends IntegrationTest {

    @Test
    void shouldUpdateOrderStatus() {
        // given
        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": { "TV-55-SAM-QLED": 1 }
            }
            """);

        assumeThat(httpResponse)
            .containsKey("orderId")
            .containsEntry("status", "PENDING");

        String orderId = httpResponse.get("orderId").toString();

        // when
        stringKafkaTemplate.send("order-shipped", """
            {
                "orderId": "%s",
                "username": "john_doe",
                "trackingNumber": "1234567890",
                "carrier": "FedEx",
                "shippedAt": "2024-08-17T14:30:00Z",
                "estimatedDelivery": "2024-08-20T18:00:00Z"
            }
            """.formatted(orderId)).join();

        // then
        await()
            .pollInterval(ofMillis(100))
            .untilAsserted(() -> {
                var resp = sendGetRequest("/v1/orders/" + orderId);
                then(resp)
                    .containsEntry("status", "SHIPPED");
            });
    }

}
