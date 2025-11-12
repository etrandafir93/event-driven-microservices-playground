package io.github.etr.playground.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
class OpenApiConfig {

    @Bean
    OpenAPI orderServiceOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Order Service API")
                .description("Event-driven microservice for managing customer orders. " +
                    "Creates orders and publishes events to Kafka for downstream processing.")
                .version("1.0.0"))
            .addServersItem(new Server()
                .url("http://localhost:8081")
                .description("Local Development Server"));
    }
}
