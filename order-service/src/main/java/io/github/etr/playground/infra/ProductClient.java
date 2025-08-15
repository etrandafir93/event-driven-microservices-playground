package io.github.etr.playground.infra;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import io.github.etr.playground.application.annotations.Adapter;
import io.github.etr.playground.domain.Product;
import io.github.etr.playground.domain.ProductCatalog;
import io.github.etr.playground.domain.ProductSku;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
@RequiredArgsConstructor
class ProductClient implements ProductCatalog {

    private final Tracer tracer;

    // dummy impl
    private final Map<ProductSku, Product> products = Map.of(new ProductSku("TV-55-SAM-QLED"),
        new Product("TV-55-SAM-QLED", "Samsung QLED 55 TV", new BigDecimal(1200.00)), new ProductSku("PHN-APL-IP15-BLK-128"),
        new Product("PHN-APL-IP15-BLK-128", "Apple iPhone 15 Black 128GB", new BigDecimal(999.00)), new ProductSku("LTP-DEL-XPS13-512"),
        new Product("LTP-DEL-XPS13-512", "Dell XPS 13 Laptop 512GB SSD", new BigDecimal(1499.00)));

    @Override
    @SneakyThrows
    public Optional<Product> findBySku(@SpanTag("sku") ProductSku sku) {
        log.info("Product Client: Finding product by SKU: {}", sku);

        long artificialDelayMs = ThreadLocalRandom.current()
            .nextLong(50, 150);
        Thread.sleep(artificialDelayMs);

        return Optional.ofNullable(products.get(sku));
    }

}
