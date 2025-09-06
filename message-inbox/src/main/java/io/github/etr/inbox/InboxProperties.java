package io.github.etr.inbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "inbox.relay")
class InboxProperties {

    private String delayMs = "100";
}