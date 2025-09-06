package io.github.etr.playground.spy;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.function.Function;

import io.github.etr.playground.application.SystemTime;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SystemTimeSpy implements SystemTime {

    private Function<LocalDateTime, LocalDateTime> stub = Function.identity();

    private final SystemTime delegate;

    public void callRealFunction() {
        this.stub = Function.identity();
    }

    public void now(Function<LocalDateTime, LocalDateTime> stub) {
        this.stub = stub;
    }

    @Override
    public LocalDateTime now() {
        LocalDateTime actualTime = delegate.now();
        return stub.apply(actualTime);
    }

    public static Function<LocalDateTime, LocalDateTime> rewindTo(DayOfWeek day) {
        return now -> {
            while (now.getDayOfWeek() != day) {
                now = now.minusDays(1);
            }
            return now;
        };
    }

}