package io.github.etr.playground.replenishment;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
class RetailSupplierClient {

    private final RestClient restClient;

    public RetailSupplierClient(
        RestClient.Builder restClientBuilder,
        @Value("${stock.replenishment.supplier.url}") String supplierUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(supplierUrl).build();
    }

    @NewSpan("replenishment-supplier-request")
    public void sendStockReplenishmentRequest(@SpanTag(key = "sku") String sku, int quantity) {
        log.info("requesting replenishment for {}", kv("sku", sku));
        restClient.post()
            .uri("/items/" + sku)
            .body(new StockReplenishmentRequest(sku, quantity))
            .retrieve()
            .toEntity(Void.class);
    }

    record StockReplenishmentRequest(String sku, int quantity) {}

}

