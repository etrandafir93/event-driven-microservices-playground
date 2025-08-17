package io.github.etr.playground.infra;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import io.github.etr.playground.IntegrationTest;

class CreateOrderTest extends IntegrationTest {

    @Test
    void shouldReturnOkHttpResponse() {
        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": {
                    "TV-55-SAM-QLED": 1,
                    "PHN-APL-IP15-BLK-128": 2,
                    "LTP-DEL-XPS13-512": 1
                }
            }
            """);

        then(httpResponse)
            .containsKey("orderId")
            .containsEntry("statusDescription", "Order received and pending processing");
    }

    @Test
    void shouldPublishOrderCreatedEvent() {
        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": {
                    "TV-55-SAM-QLED": 1,
                    "PHN-APL-IP15-BLK-128": 2,
                    "LTP-DEL-XPS13-512": 1
                }
            }
            """);

        String orderId = httpResponse.get("orderId").toString();
        var kafkaMessageOut = outgoingKafkaMessages.awaitForOrderCreated(orderId);

        then(kafkaMessageOut)
            .containsEntry("orderId", orderId)
            .containsEntry("customerUsername", "john_doe")
            .containsEntry("order", Map.of(
                "TV-55-SAM-QLED", 1,
                "PHN-APL-IP15-BLK-128", 2,
                "LTP-DEL-XPS13-512", 1
            ));
    }

    @Test
    void shouldHandleCustomerNotFound() {
        var response = sendPostRequest("/v1/orders", """
            {
                "username": "anonymous_user",
                "products": {
                    "TV-55-SAM-QLED": 1,
                    "PHN-APL-IP15-BLK-128": 2,
                    "LTP-DEL-XPS13-512": 1
                }
            }
            """);

        then(response)
            .containsEntry("status", 404)
            .containsEntry("error", "Customer not found for username: anonymous_user");
    }

    @Test
    void shouldHandleProductNotFound() {
        var response = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": {
                    "UNKNOWN-SKU-404": 1
                }
            }
            """);

        then(response)
            .containsEntry("status", 404)
            .containsEntry("error", "Product not found for SKU: UNKNOWN-SKU-404");
    }

    @NullSource
    @ValueSource(ints = { -1, 0 })
    @ParameterizedTest
    void shouldHandleInvalidQuantity(Integer quantity) {
        var response = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": {
                    "TV-55-SAM-QLED": %s
                }
            }
            """.formatted(quantity));

        then(response)
            .containsEntry("status", 400)
            .containsKey("error");
    }

    @Test
    void givenKafkaIsDown_shouldReturnOkHttpResponse() {
        givenKafkaIsDown();

        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": {
                    "TV-55-SAM-QLED": 1,
                    "PHN-APL-IP15-BLK-128": 2,
                    "LTP-DEL-XPS13-512": 1
                }
            }
            """);

        then(httpResponse)
            .containsKey("orderId")
            .containsEntry("statusDescription", "Order received and pending processing");
    }

    @Test
    void givenKafkaIsDown_shouldEventuallySendToKafka() {
        givenKafkaIsDownFor(Duration.ofSeconds(3));

        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": {
                    "TV-55-SAM-QLED": 1,
                    "PHN-APL-IP15-BLK-128": 2,
                    "LTP-DEL-XPS13-512": 1
                }
            }
            """);

        String orderId = httpResponse.get("orderId").toString();
        var kafkaMessageOut = outgoingKafkaMessages.awaitForOrderCreated(orderId);

        then(kafkaMessageOut)
            .containsEntry("orderId", orderId)
            .containsEntry("customerUsername", "john_doe")
            .containsEntry("order", Map.of(
                "TV-55-SAM-QLED", 1,
                "PHN-APL-IP15-BLK-128", 2,
                "LTP-DEL-XPS13-512", 1
            ));
    }
}
