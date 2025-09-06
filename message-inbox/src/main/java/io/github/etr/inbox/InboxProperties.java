package io.github.etr.inbox;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "inbox")
public class InboxProperties {
    
    private Relay relay = new Relay();
    
    @Data
    public static class Relay {
        private String delayMs = "100";
    }
}