package io.github.etr.playground.application;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
class KafkaConfig {

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, Object> customKafkaListenerContainerFactory(ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);

        ContainerProperties containerProps = factory.getContainerProperties();
        containerProps.setObservationEnabled(true);
        return factory;
    }

    @Bean
    @Primary
    KafkaOperations<String, Object> kafkaOperations(ProducerFactory<?, ?> producerFactory) {
        var kt = (KafkaTemplate<String, Object>) new KafkaTemplate<>(producerFactory);
        kt.setObservationEnabled(true);
        return kt;
    }

}
