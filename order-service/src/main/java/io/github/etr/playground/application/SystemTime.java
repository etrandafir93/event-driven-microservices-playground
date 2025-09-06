package io.github.etr.playground.application;

import java.time.LocalDateTime;

@FunctionalInterface
public interface SystemTime {

    LocalDateTime now();

}
