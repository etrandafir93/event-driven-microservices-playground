package io.github.etr.playground.domain.loyalty;

import static io.github.etr.playground.application.SystemTimeSpy.rewindTo;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SUNDAY;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.BDDAssertions.then;
import static org.awaitility.Awaitility.await;

import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import io.github.etr.playground.IntegrationTest;
import io.github.etr.playground.domain.order.OrderCreatedEvent;

class TieredMembersTest extends IntegrationTest {

    @Autowired
    ApplicationEventPublisher eventPublisher;

    @Autowired
    TieredMembers members;

    @Test
    void shouldEarnPointsOnOrderCreatedEvent_e2e() {
        systemTimeSpy.now(rewindTo(MONDAY));

        var httpResponse = sendPostRequest("/v1/orders", """
            {
                "username": "john_doe",
                "products": {
                    "TV-55-SAM-QLED": 1
                }
            }
            """);
        assertThat(httpResponse).hasFieldOrProperty("orderId");

        await().untilAsserted(() ->
            then(members.findByUsername("john_doe")).get()
                .hasFieldOrPropertyWithValue("points", 10)
                .hasFieldOrPropertyWithValue("tier", "BRONZE"));
    }

    @Test
    void shouldEarnPointsOnOrderCreatedEvent() {
        systemTimeSpy.now(rewindTo(MONDAY));

        eventPublisher.publishEvent(
            orderCreateEvent("fred_the_frugal", 1));
        eventPublisher.publishEvent(
            orderCreateEvent("fred_the_frugal", 1));

        await().untilAsserted(() ->
           then(members.findByUsername("fred_the_frugal")).get()
             .hasFieldOrPropertyWithValue("points", 20)
             .hasFieldOrPropertyWithValue("tier", "BRONZE"));
    }


    @Test
    void shouldUpgradeTier() {
        systemTimeSpy.now(rewindTo(SUNDAY));

        eventPublisher.publishEvent(
            orderCreateEvent("simon_the_spender", 10));
        eventPublisher.publishEvent(
            orderCreateEvent("simon_the_spender", 10));
        eventPublisher.publishEvent(
            orderCreateEvent("simon_the_spender", 10));
        eventPublisher.publishEvent(
            orderCreateEvent("simon_the_spender", 10));

        await().untilAsserted(() ->
            then(members.findByUsername("simon_the_spender")).get()
                .hasFieldOrPropertyWithValue("points", 2000)
                .hasFieldOrPropertyWithValue("tier", "PLATINUM"));
    }

    private static OrderCreatedEvent orderCreateEvent(String username, int quantity) {
        return new OrderCreatedEvent(UUID.randomUUID()
            .toString(), username, Map.of("TV-55-SAM-QLED", quantity));
    }
}
