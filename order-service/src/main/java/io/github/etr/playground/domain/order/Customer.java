package io.github.etr.playground.domain.order;

import jakarta.persistence.Embeddable;

@Embeddable
public record Customer(String username, String name, String email) {
}
