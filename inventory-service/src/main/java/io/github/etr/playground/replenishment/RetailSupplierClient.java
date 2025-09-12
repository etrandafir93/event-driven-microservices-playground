package io.github.etr.playground.replenishment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.RequiredArgsConstructor;

@Component
class RetailSupplierClient {

    private final RestClient restClient;

    public RetailSupplierClient(
        RestClient.Builder restClientBuilder,
        @Value("${stock.replenishment.supplier.url}") String supplierUrl
    ) {
        this.restClient = restClientBuilder.baseUrl(supplierUrl).build();
    }

    void sendStockReplenishmentRequest(String sku, int quantity) {
        if(true)
            return;

        restClient.post()
            .uri("/items")
            .body(new StockReplenishmentRequest(sku, quantity))
            .retrieve()
            .toEntity(Void.class);
    }

    record StockReplenishmentRequest(String sku, int quantity) {}

}

