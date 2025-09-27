package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "should update shipment status to shipped"

    request {
        method PUT()
        url("/api/v1/shipments/TRACK-456/ship") {
            queryParameters {
                parameter("shippedAt", "2023-12-26T09:00:00Z")
                parameter("estimatedDeliveryDate", "2023-12-28T12:00:00Z")
            }
        }
        headers {
            accept(applicationJson())
        }
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
    }
}