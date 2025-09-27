package contracts

org.springframework.cloud.contract.spec.Contract.make {
    description "should return shipment when valid orderId is provided"

    request {
        method GET()
        url("/api/v1/shipments") {
            queryParameters {
                parameter("orderId", "ORDER-123")
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

//                FIXME: i believe that, when using @SpringBootTest(env=MOCK).
//                 the HATEAOAS links are not yet added to the response, so we cannot add them to the contract
//                 ---- is this true?

//            _links: [
//                self: [
//                    href: "http://localhost/api/v1/shipments/TRACK-456",
//                    type: "GET"
//                ],
//                deliver: [
//                    href: "http://localhost/api/v1/shipments/TRACK-456/deliver",
//                    type: "PUT"
//                ]
//            ]
        ])
        headers {
            contentType(applicationJson())
        }
    }
}