package io.github.etr.playground.application;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan("io.github.etr.playground")
@EnableJpaRepositories("io.github.etr.playground")
class JpaConfig {

}
