package io.github.etr.playground.domain;

import java.util.NoSuchElementException;
import java.util.Optional;

import io.micrometer.core.aop.MeterTag;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;

@FunctionalInterface
@Observed(name = "crm")
public interface CustomerRelationshipManagement {

    @NewSpan // <- do i always need this for dynamic tags?
    Optional<Customer> findByUsername(@SpanTag("username") String username);

    @NewSpan
    default Customer findByUsernameOrElseThrow(@SpanTag("username") String username) {
        return findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("Customer not found for username: " + username));
    }
}
