package io.github.etr.playground.inventory;

import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.MAP;
import static org.awaitility.Awaitility.await;

import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import io.github.etr.playground.IntegrationTest;
import lombok.SneakyThrows;

@SpringBootTest
class OrderCreatedEventConsumptionTest extends IntegrationTest {

    @Test
    @SneakyThrows
    void shouldSplitOrderIntoIndividualMessages() {
        sendKafkaMessageWithTypeHeader("order-created", "john_doe", """
            {
                "orderId": "test-order-123",
                "username": "john_doe",
                "order": {
                    "TV-55-SAM-QLED": 1,
                    "PHN-APL-IP15-BLK-128": 2
                }
            }
            """);

        await().untilAsserted(() ->
            then(outgoingKafkaMessages.messagesForProduct("TV-55-SAM-QLED"))
                .hasSize(1).first().asInstanceOf(MAP)
                .containsEntry("productSku", "TV-55-SAM-QLED")
                .containsEntry("orderId", "test-order-123")
                .containsEntry("username", "john_doe")
                .containsEntry("quantity", 1));

        await().untilAsserted(() ->
            then(outgoingKafkaMessages.messagesForProduct("PHN-APL-IP15-BLK-128"))
                .hasSize(1).first().asInstanceOf(MAP)
                .containsEntry("productSku", "PHN-APL-IP15-BLK-128")
                .containsEntry("orderId", "test-order-123")
                .containsEntry("username", "john_doe")
                .containsEntry("quantity", 2));
    }

}