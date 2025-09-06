package io.github.etr.playground.application;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class SystemTimeConfig {

    @Bean
    SystemTime systemTime() {
        return LocalDateTime::now;
    }

}
