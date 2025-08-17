package io.github.etr.playground.infra;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.beans.factory.annotation.Value;

import io.github.etr.playground.application.annotations.Adapter;
import io.github.etr.playground.domain.Customer;
import io.github.etr.playground.domain.CustomerRelationshipManagement;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
class CrmClient implements CustomerRelationshipManagement {

    @Value("${stub.artificial-delay.ms: 200}")
    private Long fakeDelayMs;

    private final Map<String, Customer> customers = Map.of("john_doe", new Customer("john_doe", "John DOE", "johndoe@yahoo.com"), "bad_luck_brian",
        new Customer("bad_luck_brian", "Brian BADLUCK", "brian@yahoo.com"), "third_time_tracy",
        new Customer("third_time_tracy", "Tracy THIRDTIME", "tracy@yahoo.com"));

    @Override
    @SneakyThrows
    public Optional<Customer> findByUsername(String username) {
        log.info("CRM Client: Finding customer by username: {}", username);
        Thread.sleep(Duration.ofMillis(fakeDelayMs));
        return Optional.ofNullable(customers.get(username));
    }

}
