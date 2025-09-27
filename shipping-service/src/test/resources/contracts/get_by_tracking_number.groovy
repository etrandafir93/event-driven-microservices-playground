package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "should return shipment when valid trackingNumber is provided"

    request {
        method GET()
        url("/api/v1/shipments") {
            queryParameters {
                parameter("trackingNumber", "TRACK-456")
            }
        }
        headers {
            accept(applicationJson())
        }
    }

    response {
        status OK()
        body([
            orderId: "ORDER-123",
            username: "john.doe",
            trackingNumber: "TRACK-456",
            carrier: "DHL",
            packedAt: "2023-12-25T10:00:00Z",
            estimatedShipping: "2023-12-26T08:00:00Z",
            shippedAt: "2023-12-26T09:00:00Z",
            estimatedDelivery: "2023-12-28T12:00:00Z",
            deliveredAt: null
        ])
        headers {
            contentType(applicationJson())
        }
    }
}