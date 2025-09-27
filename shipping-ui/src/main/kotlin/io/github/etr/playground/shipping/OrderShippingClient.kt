package io.github.etr.playground.shipping

import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import java.time.Instant

@Component
class OrderShippingClient(
    private val restClient: RestClient
) {

    fun get(orderId: String? = null, trackingNumber: String? = null): OrderShipmentProjection {
        val queryParam = when {
            orderId != null -> "orderId" to orderId
            trackingNumber != null -> "trackingNumber" to trackingNumber
            else -> throw IllegalArgumentException("Either orderId or trackingNumber must be provided")
        }

        return restClient.get()
            .uri {
                it.path("/api/v1/shipments")
                    .queryParam(queryParam.first, queryParam.second)
                    .build()
            }
            .header("Accept", "application/json")
            .retrieve()
            .body(OrderShipmentProjection::class.java)!!
    }

    fun put(href: String) {
        restClient.put()
            .uri(href)
            .header("Accept", "application/json")
            .retrieve()
            .toBodilessEntity()
    }
}

data class OrderShipmentProjection(
    val orderId: String,
    val username: String,
    val trackingNumber: String,
    val carrier: String,
    val packedAt: Instant?,
    val estimatedShipping: Instant?,
    val shippedAt: Instant?,
    val estimatedDelivery: Instant?,
    val deliveredAt: Instant?,
    val _links: Map<String, Link>?
)

data class Link(
    val href: String,
    val type: String
)