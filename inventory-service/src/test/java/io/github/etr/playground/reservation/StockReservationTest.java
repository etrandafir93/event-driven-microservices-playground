package io.github.etr.playground.reservation;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.etr.playground.IntegrationTest;
import lombok.SneakyThrows;

class StockReservationTest extends IntegrationTest {

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

    @SneakyThrows
    @ParameterizedTest(name = "ordering {0}x{1} should result in {2}")
    @CsvSource(value = {
        "   9 | DUMMY-SKU-10 | stock-reserved    ",
        "  10 | DUMMY-SKU-10 | stock-reserved    ",
        "  11 | DUMMY-SKU-10 | stock-unavailable ",
        " 100 | DUMMY-SKU-10 | stock-unavailable ",
        "   1 | UNKNOWN-SKU  | stock-unavailable "
    }, delimiter = '|')
    void shouldProcessAndRouteOutDomainEvent(int quantity, String sku, String eventOut) {
        sendKafkaMessage("order-created", "john_doe", """
            {
                "orderId": "test-order-123",
                "username": "john_doe",
                "order": {
                    "%s": %s
                }
            }
            """.formatted(sku, quantity));

        Thread.sleep(10_000);

        var stockUnavailableEvt = await().until(() ->
                outgoingKafkaMessages.messagesFor(eventOut), hasCount(1));

        then(stockUnavailableEvt.getFirst())
            .containsEntry("itemSku", sku)
            .containsEntry("orderId", "test-order-123")
            .containsEntry("quantity", quantity);
    }

    private static Predicate<List<Map<String, Object>>> hasCount(int count) {
        return it -> it.size() == count;
    }

}