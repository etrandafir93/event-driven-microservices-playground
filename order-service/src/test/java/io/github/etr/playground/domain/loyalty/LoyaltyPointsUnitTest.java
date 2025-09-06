package io.github.etr.playground.domain.loyalty;

import static io.github.etr.playground.spy.SystemTimeSpy.rewindTo;
import static java.time.DayOfWeek.FRIDAY;
import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.THURSDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static java.util.Arrays.stream;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.BDDAssertions.then;
import static org.assertj.core.api.InstanceOfAssertFactories.LOCAL_DATE_TIME;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

import io.github.etr.playground.spy.SystemTimeSpy;

class LoyaltyPointsUnitTest {

    private final SystemTimeSpy systemTime = new SystemTimeSpy(LocalDateTime::now);

    private final TieredMembershipService service = new TieredMembershipService(null, systemTime);

    @DisplayName("Ordering {x} items on a {y} day should earn you {z} points")
    @ParameterizedTest(name = "{1}x items on {0} => {2} pts")
    @CsvSource(value = {
        "MONDAY   |  1 | 10",
        "MONDAY   |  2 | 20",
        "MONDAY   | 10 | 100",
        "TUESDAY  |  1 | 10",
        "TUESDAY  |  2 | 20",
        "TUESDAY  | 10 | 100",
        "THURSDAY |  1 | 30",
        "THURSDAY |  2 | 60",
        "THURSDAY | 10 | 300",
        "FRIDAY   |  1 | 30",
        "FRIDAY   |  2 | 60",
        "FRIDAY   | 10 | 300",
        "SATURDAY |  1 | 50",
        "SATURDAY |  2 | 100",
        "SATURDAY | 10 | 500",
        "SUNDAY   |  1 | 50",
        "SUNDAY   |  2 | 100",
        "SUNDAY   | 10 | 500",
    }, delimiter = '|')
    void shouldCalculateLoyaltyPointsBasedOnDayOfWeek(DayOfWeek dayOfWeek, int itemsCount, int expectedPoints) {
        systemTime.now(rewindTo(dayOfWeek));

        var order = Map.of("SKU-DUMMY-ITEM", itemsCount);
        int actualPoints = service.pointsToEarn(order);

        then(actualPoints)
            .isEqualTo(expectedPoints);
    }

    // PBT-style test, it was fun but is it too much?
    @MethodSource
    @ParameterizedTest(name = "{1}x {0}")
    void orderingLaterOnInTheWeek_shouldEarnMorePoints(String sku, int quantity) {
        Map<DayOfWeek, Integer> ptsByDay = stream(DayOfWeek.values()).collect(
            toMap(identity(), day -> {
                systemTime.now(rewindTo(day));
                return service.pointsToEarn(Map.of(sku, quantity));
            }));

        prettyPrint(sku, quantity, ptsByDay);

        assertAll(
            () -> assertTrue(ptsByDay.get(MONDAY) <= ptsByDay.get(TUESDAY)),
            () -> assertTrue(ptsByDay.get(TUESDAY) <= ptsByDay.get(WEDNESDAY)),
            () -> assertTrue(ptsByDay.get(WEDNESDAY) <= ptsByDay.get(THURSDAY)),
            () -> assertTrue(ptsByDay.get(THURSDAY) <= ptsByDay.get(FRIDAY)),
            () -> assertTrue(ptsByDay.get(FRIDAY) <= ptsByDay.get(SATURDAY)),
            () -> assertTrue(ptsByDay.get(SATURDAY) <= ptsByDay.get(SUNDAY))
        );
    }

    private static void prettyPrint(String sku, int quantity, Map<DayOfWeek, Integer> ptsByDay) {
        String prettyString = ptsByDay.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(it -> "%sp on %s".formatted(it.getValue(), it.getKey().name().substring(0, 3)))
            .collect(joining(", "));

        System.out.printf("ordering %sx %s earns you %s\n", quantity, sku, prettyString);
    }

    static Stream<Arguments> orderingLaterOnInTheWeek_shouldEarnMorePoints() {
        return Stream.generate(() -> Arguments.of(randomSku(), randomQuantity()))
            .limit(100);
    }

    static int randomQuantity() {
        return ThreadLocalRandom.current()
            .nextInt(1, 10);
    }

    static String randomSku() {
        return "SKU-" + UUID.randomUUID()
            .toString()
            .substring(0, 8)
            .toUpperCase();
    }

}