package io.github.etr.playground.domain.order;

import java.util.NoSuchElementException;
import java.util.Optional;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;

@FunctionalInterface
public interface ProductCatalog {

    @NewSpan("product-catalog-client")
    Optional<Product> findBySku(@SpanTag(key = "sku", expression = "value") ProductSku sku);

    @NewSpan("product-catalog-client")
    default Product findBySkuOrElseThrow(@SpanTag(key = "sku", expression = "value") ProductSku sku) {
        return findBySku(sku).orElseThrow(() -> new NoSuchElementException("Product not found for SKU: " + sku.value()));
    }
}
