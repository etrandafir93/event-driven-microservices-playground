package io.github.etr.playground.shippingui

import io.github.etr.playground.shipping.OrderShippingClient
import io.github.etr.playground.shipping.OrderShippingClientConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties
import java.time.Instant

@SpringBootTest(
    webEnvironment = WebEnvironment.MOCK,
    classes = [OrderShippingClient::class, OrderShippingClientConfig::class]
)
@AutoConfigureStubRunner(
    stubsMode = StubRunnerProperties.StubsMode.LOCAL,
    ids = ["io.github.etr:shipping-service:+:stubs:8083"]
)
class OrderShippingClientContractTest {

    @Autowired
    private lateinit var client: OrderShippingClient

    @Test
    fun `should find by order id`() {
        assertThat(client.get(orderId = "ORDER-123"))
            .hasFieldOrPropertyWithValue("orderId", "ORDER-123")
            .hasFieldOrPropertyWithValue("username", "john.doe")
            .hasFieldOrPropertyWithValue("trackingNumber", "TRACK-456")
            .hasFieldOrPropertyWithValue("carrier", "DHL")
            .hasFieldOrPropertyWithValue("packedAt", Instant.parse("2023-12-25T10:00:00Z"))
            .hasFieldOrPropertyWithValue("estimatedShipping", Instant.parse("2023-12-26T08:00:00Z"))
            .hasFieldOrPropertyWithValue("shippedAt", Instant.parse("2023-12-26T09:00:00Z"))
            .hasFieldOrPropertyWithValue("estimatedDelivery", Instant.parse("2023-12-28T12:00:00Z"))
    }

    @Test
    fun `should find by tracking number`() {
        assertThat(client.get(trackingNumber = "TRACK-456"))
            .hasFieldOrPropertyWithValue("orderId", "ORDER-123")
            .hasFieldOrPropertyWithValue("username", "john.doe")
            .hasFieldOrPropertyWithValue("trackingNumber", "TRACK-456")
            .hasFieldOrPropertyWithValue("carrier", "DHL")
            .hasFieldOrPropertyWithValue("packedAt", Instant.parse("2023-12-25T10:00:00Z"))
            .hasFieldOrPropertyWithValue("estimatedShipping", Instant.parse("2023-12-26T08:00:00Z"))
            .hasFieldOrPropertyWithValue("shippedAt", Instant.parse("2023-12-26T09:00:00Z"))
            .hasFieldOrPropertyWithValue("estimatedDelivery", Instant.parse("2023-12-28T12:00:00Z"))
    }

    @Test
    fun `should pack order`() {
        val uri = "/api/v1/shipments/TRACK-456/pack" +
                "?packedAt=2023-12-25T10:00:00Z" +
                "&estimatedShippingDate=2023-12-26T08:00:00Z"

        assertDoesNotThrow { client.put(uri) }
    }

    @Test
    fun `should ship order`() {
        val uri = "/api/v1/shipments/TRACK-456/ship" +
                "?shippedAt=2023-12-26T09:00:00Z" +
                "&estimatedDeliveryDate=2023-12-28T12:00:00Z"

        assertDoesNotThrow { client.put(uri) }
    }

    @Test
    fun `should deliver order`() {
        val uri = "/api/v1/shipments/TRACK-456/deliver" +
                "?deliveredAt=2023-12-28T14:00:00Z"

        assertDoesNotThrow { client.put(uri) }
    }

}