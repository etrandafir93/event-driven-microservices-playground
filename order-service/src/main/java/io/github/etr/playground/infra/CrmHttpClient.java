package io.github.etr.playground.infra;

import java.util.Map;
import java.util.Optional;

import io.github.etr.playground.application.Adapter;
import io.github.etr.playground.domain.Customer;
import io.github.etr.playground.domain.CustomerRelationshipManagement;

@Adapter
class CrmHttpClient implements CustomerRelationshipManagement {

    private final Map<String, Customer> customers = Map.of(
        "john_doe", new Customer("john_doe", "John DOE", "johndoe@yahoo.com")
    ); // dummy impl

    @Override
    public Optional<Customer> findByUsername(String username) {
        return Optional.ofNullable(customers.get(username));
    }
}
