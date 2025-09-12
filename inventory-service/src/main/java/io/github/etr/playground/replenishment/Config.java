package io.github.etr.playground.replenishment;

import java.util.HashMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("replenishmentConfig")
public class Config {

    @Bean
    ReplenishmentThreshold replenishmentThreshold() {
        return new ReplenishmentThreshold(100, new HashMap<>());
    }
}
