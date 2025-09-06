package io.github.etr.playground.application;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.function.Function;

public class SystemTimeSpy extends SystemTime {

    private Function<LocalDateTime, LocalDateTime> stub = Function.identity();

    public static Function<LocalDateTime, LocalDateTime> rewindTo(DayOfWeek day) {
        return now -> {
            while (now.getDayOfWeek() != day) {
                now = now.minusDays(1);
            }
            return now;
        };
    }

    public void reset() {
        this.stub = Function.identity();
    }

    public void now(Function<LocalDateTime, LocalDateTime> stub) {
        this.stub = stub;
    }

    @Override
    public LocalDateTime get() {
        return stub.apply(super.get());
    }
}