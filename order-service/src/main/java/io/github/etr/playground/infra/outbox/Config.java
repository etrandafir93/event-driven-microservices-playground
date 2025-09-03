package io.github.etr.playground.infra.outbox;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
class Config {

    @Bean
    KafkaTemplate<String, String> stringKafkaTemplate(ProducerFactory<?, ?> producerFactory) {
        return (KafkaTemplate<String, String>) new KafkaTemplate<>(producerFactory,
            Map.of(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName()));
    }

}
