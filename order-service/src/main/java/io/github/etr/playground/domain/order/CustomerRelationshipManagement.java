package io.github.etr.playground.domain.order;

import java.util.NoSuchElementException;
import java.util.Optional;

import io.micrometer.tracing.annotation.NewSpan;
import io.micrometer.tracing.annotation.SpanTag;

@FunctionalInterface
public interface CustomerRelationshipManagement {

    @NewSpan("crm-client")
    Optional<Customer> findByUsername(@SpanTag("username") String username);

    @NewSpan("crm-client")
    default Customer findByUsernameOrElseThrow(@SpanTag("username") String username) {
        return findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("Customer not found for username: " + username));
    }
}
