package io.github.etr.playground.shipping.domain;

import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.etr.playground.IntegrationTest;

class OrderShipmentsCommandHandlerTest extends IntegrationTest {

    @Autowired
    private OrderShipmentsRepository shipmentsRepo;

    @BeforeEach
    void setUp() {
        shipmentsRepo.deleteAll();

    }

    @Test
    void should_create_new_shipment() {
        super.sendKafkaMessage("stock-reserved", "order-123", """
            {
                "orderId": "order-123",
                "username": "john_doe"
            }
            """);

        await().untilAsserted((() ->
            then(shipmentsRepo.findByOrderId("order-123"))
                .isPresent().get()
                .hasFieldOrPropertyWithValue("orderId", "order-123")
                .hasFieldOrPropertyWithValue("username", "john_doe")
                .hasFieldOrProperty("trackingNumber")));
    }

}