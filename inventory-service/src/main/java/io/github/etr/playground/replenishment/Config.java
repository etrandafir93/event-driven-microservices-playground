package io.github.etr.playground.replenishment;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration("replenishmentConfig")
@EnableConfigurationProperties(ReplenishmentThreshold.class)
class Config {

}
