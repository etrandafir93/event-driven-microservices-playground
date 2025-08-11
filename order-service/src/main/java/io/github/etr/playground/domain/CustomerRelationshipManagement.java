package io.github.etr.playground.domain;

import java.util.NoSuchElementException;
import java.util.Optional;

@FunctionalInterface
public interface CustomerRelationshipManagement {

    Optional<Customer> findByUsername(String username);

    default Customer findByUsernameOrElseThrow(String username) {
        return findByUsername(username)
            .orElseThrow(() -> new NoSuchElementException("Customer not found for username: " + username));
    }
}
