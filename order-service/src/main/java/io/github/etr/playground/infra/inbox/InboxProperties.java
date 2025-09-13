package io.github.etr.playground.infra.inbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "inbox.relay")
class InboxProperties {

    private String delayMs = "100";
}