package io.github.etr.playground.shipping.infra;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.etr.playground.application.JacksonConfig;
import io.github.etr.playground.shipping.domain.OrderShipmentQueries;
import io.github.etr.playground.shipping.domain.OrderShipmentsCommandHandler;
import io.restassured.module.mockmvc.RestAssuredMockMvc;

@AutoConfigureMessageVerifier
@SpringBootTest(webEnvironment = WebEnvironment.MOCK,
    classes = {OrderShipmentsRestController.class, JacksonConfig.class})
public class ContractTest {

    @MockitoBean
    private OrderShipmentsCommandHandler commands;

    @MockitoBean
    private OrderShipmentQueries queries;

    @Autowired
    private OrderShipmentsRestController api;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        RestAssuredMockMvc.standaloneSetup(
            MockMvcBuilders.standaloneSetup(api)
                .setMessageConverters(
                    new MappingJackson2HttpMessageConverter(objectMapper)));

        given(queries.findByOrderId(any(), any()))
            .willReturn(Optional.of(anOrderShipment()));

        given(queries.findByTrackingNumber(any(), any()))
            .willReturn(Optional.of(anOrderShipment()));
    }

    private static OrderShipmentProjection anOrderShipment() {
        return OrderShipmentProjection.builder()
            .orderId("ORDER-123")
            .username("john.doe")
            .trackingNumber("TRACK-456")
            .carrier("DHL")
            .packedAt(Instant.parse("2023-12-25T10:00:00Z"))
            .estimatedShipping(Instant.parse("2023-12-26T08:00:00Z"))
            .shippedAt(Instant.parse("2023-12-26T09:00:00Z"))
            .estimatedDelivery(Instant.parse("2023-12-28T12:00:00Z"))
            .build();
    }
}
