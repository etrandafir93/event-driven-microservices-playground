package io.github.etr.playground.reservation;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.etr.playground.IntegrationTest;

class OrderCreatedListenerTest extends IntegrationTest {

    @Test
    void shouldSplitOrderIntoIndividualMessages() {
        sendKafkaMessage("order-created", "john_doe", """
            {
                "orderId": "test-order-123",
                "username": "john_doe",
                "order": {
                    "TV-55-SAM-QLED": 1,
                    "PHN-APL-IP15-BLK-128": 2
                }
            }
            """);

        var itemOrderEvts = await().until(() ->
            outgoingKafkaMessages.messagesFor("item-ordered"), hasCount(2));

        then(itemOrderEvts)
            .hasSize(2)
            .contains(Map.of(
                "itemSku", "TV-55-SAM-QLED",
                "orderId", "test-order-123",
                "username", "john_doe",
                "quantity", 1
            ))
            .contains(Map.of(
                "itemSku", "PHN-APL-IP15-BLK-128",
                "orderId", "test-order-123",
                "username", "john_doe",
                "quantity", 2
            ));
    }

}