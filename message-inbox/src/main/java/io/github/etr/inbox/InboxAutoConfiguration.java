package io.github.etr.inbox;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@AutoConfiguration
@EnableConfigurationProperties(InboxProperties.class)
@EnableJpaRepositories
@EntityScan
@EnableScheduling
public class InboxAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    InboxProcessor inboxProcessor(InboxRepo inboxRepo, ApplicationContext context, ApplicationEventPublisher eventPublisher) {
        return new InboxProcessor(inboxRepo, context, eventPublisher);
    }

    @Bean
    @ConditionalOnMissingBean
    InboxRelay inboxRelay(InboxRepo inboxRepo, InboxProcessor inboxProcessor) {
        return new InboxRelay(inboxRepo, inboxProcessor);
    }
}