package io.github.etr.playground.domain;

import java.util.NoSuchElementException;
import java.util.Optional;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;

@FunctionalInterface
public interface ProductCatalog {

    @NewSpan
    Optional<Product> findBySku(@SpanTag(key = "sku", expression = "value") ProductSku sku);

    @NewSpan
    default Product findBySkuOrElseThrow(@SpanTag(key = "sku", expression = "value") ProductSku sku) {
        return findBySku(sku).orElseThrow(() -> new NoSuchElementException("Product not found for SKU: " + sku.value()));
    }
}
