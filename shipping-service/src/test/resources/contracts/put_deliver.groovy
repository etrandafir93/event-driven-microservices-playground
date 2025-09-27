package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "should update shipment status to delivered"

    request {
        method PUT()
        url("/api/v1/shipments/TRACK-456/deliver") {
            queryParameters {
                parameter("deliveredAt", "2023-12-28T14:00:00Z")
            }
        }
        headers {
            contentType(applicationJson())
        }
    }

    response {
        status OK()
        headers {
            contentType(applicationJson())
        }
    }
}