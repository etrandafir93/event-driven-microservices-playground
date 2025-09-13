package io.github.etr.playground.reservation;

import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.etr.playground.IntegrationTest;

class StockReservation_OutcomePublisherTest extends IntegrationTest {

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

        var stockUnavailableEvt = await().until(() ->
                outgoingKafkaMessages.messagesFor(eventOut), hasCount(1));

        then(stockUnavailableEvt.getFirst())
            .containsEntry("itemSku", sku)
            .containsEntry("orderId", "test-order-123")
            .containsEntry("stockRequested", quantity);
    }

}