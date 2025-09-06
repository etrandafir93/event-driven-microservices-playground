package io.github.etr.playground.application;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import org.springframework.stereotype.Component;

@Component
public class SystemTime implements Supplier<LocalDateTime> {
    @Override
    public LocalDateTime get() {
        return LocalDateTime.now();
    }
}
