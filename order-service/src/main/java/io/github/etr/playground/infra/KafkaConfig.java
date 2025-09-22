package io.github.etr.playground.infra;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
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

}
