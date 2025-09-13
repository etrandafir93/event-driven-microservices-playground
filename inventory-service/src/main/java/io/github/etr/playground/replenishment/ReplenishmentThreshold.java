package io.github.etr.playground.replenishment;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "stock.replenishment.threshold")
class ReplenishmentThreshold {

    private int defaultValue = 100;
    private List<SkuThreshold> skuSpecific = new ArrayList<>();

    int threshold(String sku) {
        return skuSpecific.stream()
            .filter(it -> sku.equals(it.sku()))
            .map(SkuThreshold::value)
            .findFirst()
            .orElse(defaultValue);
    }

    @Data
    static class SkuThreshold {
        private String sku;
        private int value;
    }
}
