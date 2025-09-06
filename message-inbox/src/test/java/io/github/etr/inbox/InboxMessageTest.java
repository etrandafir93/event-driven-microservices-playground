package io.github.etr.inbox;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("TODO!")
@SpringBootTest(classes = InboxAutoConfiguration.class)
class InboxMessageTest {

    @Test
    void contextLoads() {
        assertThat(true).isTrue();
    }
}