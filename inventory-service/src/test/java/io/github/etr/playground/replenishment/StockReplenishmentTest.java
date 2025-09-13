package io.github.etr.playground.replenishment;

import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import io.github.etr.playground.IntegrationTest;

class StockReplenishmentTest extends IntegrationTest {

    @Test
    void shouldRequestStockReplenishment_whenStockIsBelowThreshold() {
        sendKafkaMessage("stock-reserved", "john_doe", """
            {
                "orderId": "test-order-123",
                "itemSku": "DUMMY-SKU-10k",
                "stockRequested": 2,
                "stockAvailable": 99
            }
            """);

        await().untilAsserted(() ->
            verify(postRequestedFor(urlPathMatching("/items/DUMMY-SKU-10k"))));
    }

    @Test
    void shouldNotRequestStockReplenishment_whenStockIsAboveThreshold() {
        sendKafkaMessage("stock-reserved", "john_doe", """
            {
                "orderId": "test-order-123",
                "itemSku": "DUMMY-SKU-10k",
                "stockRequested": 1,
                "stockAvailable": 100
            }
            """);

        await().during(ofSeconds(5)).untilAsserted(() ->
            verify(0, postRequestedFor(urlPathMatching("/items/DUMMY-SKU-10k"))));
    }
}