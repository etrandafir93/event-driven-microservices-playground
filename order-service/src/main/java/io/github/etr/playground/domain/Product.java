package io.github.etr.playground.domain;

import java.math.BigDecimal;

import jakarta.persistence.Embeddable;

@Embeddable
public record Product(String sku, String productName, BigDecimal price) {
}
