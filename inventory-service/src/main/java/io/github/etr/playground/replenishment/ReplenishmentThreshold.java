package io.github.etr.playground.replenishment;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
class ReplenishmentThreshold {

    private Integer defaultValue = 100;

    private Map<String, Integer> skuSpecific = new HashMap<>();

    int threshold(String sku) {
        return skuSpecific.getOrDefault(sku, defaultValue);
    }
}
