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
import static java.util.stream.Collectors.toMap;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Map;

import io.github.etr.playground.spy.SystemTimeSpy;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

class TieredMembersPropertyBaseTest {

    private final SystemTimeSpy systemTime = new SystemTimeSpy(LocalDateTime::now);

    private final TieredMembershipService service = new TieredMembershipService(null, systemTime);

    @Property
    boolean orderingLaterInTheWeek_shouldEarnEqualOrMorePoints(
        @ForAll("skus") String sku,
        @ForAll @IntRange(min = 1, max = 10) int quantity
    ) {
        var order = Map.of(sku, quantity);

        var pointsByDay = stream(DayOfWeek.values())
            .collect(toMap(identity(), day -> getPointsForDay(order, day)));

        return pointsByDay.get(MONDAY) <= pointsByDay.get(TUESDAY)
            && pointsByDay.get(TUESDAY) <= pointsByDay.get(WEDNESDAY)
            && pointsByDay.get(WEDNESDAY) <= pointsByDay.get(THURSDAY)
            && pointsByDay.get(THURSDAY) <= pointsByDay.get(FRIDAY)
            && pointsByDay.get(FRIDAY) <= pointsByDay.get(SATURDAY)
            && pointsByDay.get(SATURDAY) <= pointsByDay.get(SUNDAY);
    }

    @Property
    boolean pointsShouldScaleLinearly_withQuantity(
        @ForAll("skus") String sku,
        @ForAll @IntRange(min = 1, max = 10) int quantity1,
        @ForAll @IntRange(min = 1, max = 10) int quantity2,
        @ForAll DayOfWeek day
    ) {
        systemTime.now(rewindTo(day));

        // split order
        int points1 = service.pointsToEarn(Map.of(sku, quantity1));
        int points2 = service.pointsToEarn(Map.of(sku, quantity2));

        // single order
        int pointsSum = service.pointsToEarn(Map.of(sku, quantity1 + quantity2));

        return points1 + points2 == pointsSum;
    }

    @Property
    boolean sameDayShouldYeldSamePoints_regardlessOfWeekOfYear(
        @ForAll("skus") String sku,
        @ForAll @IntRange(min = 1, max = 10) int quantity,
        @ForAll DayOfWeek day
    ) {
        systemTime.callRealFunction();
        int pointsNow = service.pointsToEarn(Map.of(sku, quantity));

        systemTime.now(now -> now.minusWeeks(1));
        int pointsLastWeek = service.pointsToEarn(Map.of(sku, quantity));

        systemTime.now(now -> now.minusWeeks(2));
        int pointsTwoWeeksAgo = service.pointsToEarn(Map.of(sku, quantity));

        return pointsNow == pointsLastWeek
            && pointsLastWeek == pointsTwoWeeksAgo;
    }

    private int getPointsForDay(Map<String, Integer> order, DayOfWeek day) {
        systemTime.now(rewindTo(day));
        return service.pointsToEarn(order);
    }

    @Provide
    Arbitrary<String> skus() {
        return Arbitraries.strings()
            .alpha()
            .numeric()
            .ofMinLength(3)
            .ofMaxLength(10)
            .map(s -> "SKU-" + s.toUpperCase());
    }

}
