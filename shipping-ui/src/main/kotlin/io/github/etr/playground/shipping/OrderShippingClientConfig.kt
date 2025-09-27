package io.github.etr.playground.shipping

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class OrderShippingClientConfig {

    @Bean
    fun restClient(@Value("\${shipping.service.url}") baseUrl: String): RestClient {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .build()
    }
}