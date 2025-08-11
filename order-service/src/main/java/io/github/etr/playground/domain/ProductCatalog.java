package io.github.etr.playground.domain;

import java.util.NoSuchElementException;
import java.util.Optional;

@FunctionalInterface
public interface ProductCatalog {
    Optional<Product> findBySku(ProductSku sku);
    default Product findBySkuOrElseThrow(ProductSku sku) {
        return findBySku(sku)
                .orElseThrow(() -> new NoSuchElementException("Product not found for SKU: " + sku.value()));
    }
}
