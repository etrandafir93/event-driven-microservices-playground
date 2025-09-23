package io.github.etr.playground;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicNode;
import org.junit.jupiter.api.TestFactory;

import com.fasterxml.jackson.databind.JsonNode;

class OrderShipmentApiDiscoveryTest extends IntegrationTest {

    @TestFactory
    @DisplayName("HATEAOAS API discovery")
    List<DynamicNode> shouldProgressThroughLinks() {
        List<DynamicNode> tests = new ArrayList<>();

        String trackingNumber = givenNewOrderShipment();
        String originalGetPath = "/shipments?trackingNumber=" + trackingNumber;
        JsonNode response = getJson(originalGetPath);

        final JsonNode finalResponse = response;
        tests.add(dynamicTest("GET " + originalGetPath, () -> then(finalResponse.get("_links")
            .asOptional()).isPresent()));

        boolean canBeSelf = false;
        while (true) {
            var currentResponseLinks = extractLinks(response);

            var nextLinkOpt = discoverNextLink(currentResponseLinks, canBeSelf);
            if (nextLinkOpt.isEmpty()) {
                break;
            }
            var nextLink = nextLinkOpt.get();

            response = httpRequestFromLink(nextLink);
            final var newResponse = response;

            tests.add(dynamicTest(prettyPrintLink(nextLink), () -> then(newResponse.get("_links")
                .asOptional()).isPresent()));

            canBeSelf = !canBeSelf;
        }

        return tests;
    }

    private static Optional<JsonNode> discoverNextLink(List<Link> links, boolean canBeSelf) {
        return links.stream()
            .filter(it -> canBeSelf || !it.isSelf())
            .map(Link::jsonNode)
            .findFirst();
    }

    record Link(String rel, JsonNode jsonNode) {

        boolean isSelf() {
            return "self".equals(rel);
        }
    }

    private JsonNode httpRequestFromLink(JsonNode link) {
        String method = link.get("type")
            .asText("GET");
        String path = link.get("href")
            .asText()
            .replace("/api/v1", "");
        return httpRequest(method, path);
    }

    private String prettyPrintLink(JsonNode link) {
        String method = link.get("type")
            .asText("GET");
        String path = link.get("href")
            .asText()
            .replace("/api/v1", "");
        return "%s %s".formatted(method, path);
    }

    private String givenNewOrderShipment() {
        sendKafkaMessage("stock-reserved", "order-101", """
            {
                "orderId": "order-101",
                "username": "john_doe",
                "itemSku": "DUMMY-SKU-10"
            }
            """);

        var trackingNumber = await().until(() -> sendGetRequest("/shipments?orderId=order-101").get("trackingNumber"), Objects::nonNull);
        return trackingNumber.toString();
    }

    private static List<Link> extractLinks(JsonNode json) {
        Map<String, JsonNode> links = new HashMap<>();
        json.get("_links")
            .fields()
            .forEachRemaining(it -> links.put(it.getKey(), it.getValue()));

        return links.entrySet()
            .stream()
            .map(it -> new Link(it.getKey(), it.getValue()))
            .toList();
    }
}
