package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "should update shipment status to packed"

    request {
        method PUT()
        url("/api/v1/shipments/TRACK-456/pack") {
            queryParameters {
                parameter("packedAt", "2023-12-25T10:00:00Z")
                parameter("estimatedShippingDate", "2023-12-26T08:00:00Z")
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