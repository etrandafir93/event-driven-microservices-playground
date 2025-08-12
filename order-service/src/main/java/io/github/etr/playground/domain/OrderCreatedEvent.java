package io.github.etr.playground.domain;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

public record OrderCreatedEvent(Order order) {
}
