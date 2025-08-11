package io.github.etr.playground.infra;

import static org.assertj.core.api.BDDAssertions.then;

import org.assertj.core.api.Assertions;
import org.assertj.core.api.BDDAssertions;
import org.junit.jupiter.api.Test;

import io.github.etr.IntegrationTest;

class CreateOrderTest extends IntegrationTest {

    @Test
    void shouldCreateAnOrder() {
        var response = givenPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": [
                    "TV-55-SAM-QLED",
                    "PHN-APL-IP15-BLK-128",
                    "LTP-DEL-XPS13-512"
                ]
            }
            """);

        then(response)
            .containsKey("orderId")
            .containsEntry("status", "Order received and pending processing");
    }

    @Test
    void shouldHandleCustomerNotFound() {
        var response = givenPostRequest("/v1/orders", """
            {
                "username": "anonymous_user",
                "products": [
                    "TV-55-SAM-QLED",
                    "PHN-APL-IP15-BLK-128",
                    "LTP-DEL-XPS13-512"
                ]
            }
            """);

        then(response)
            .containsEntry("status", 404)
            .containsEntry("error", "Customer not found for username: anonymous_user");
    }

    @Test
    void shouldHandleProductNotFound() {
        var response = givenPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": [
                    "UNKNOWN-SKU-404"
                ]
            }
            """);

        then(response)
            .containsEntry("status", 404)
            .containsEntry("error", "Product not found for SKU: UNKNOWN-SKU-404");
    }

}
