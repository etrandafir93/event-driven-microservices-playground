package io.github.etr.playground.infra;

import java.util.Map;
import java.util.Optional;

import io.github.etr.playground.application.annotations.Adapter;
import io.github.etr.playground.domain.Customer;
import io.github.etr.playground.domain.CustomerRelationshipManagement;

@Adapter
class CrmHttpClient implements CustomerRelationshipManagement {

    private final Map<String, Customer> customers = Map.of(
        "john_doe", new Customer("john_doe", "John DOE", "johndoe@yahoo.com"),
        "bad_luck_brian", new Customer("bad_luck_brian", "Brian BADLUCK", "brian@yahoo.com"),
        "third_time_tracy", new Customer("third_time_tracy", "Tracy THIRDTIME", "tracy@yahoo.com")
    );

    @Override
    public Optional<Customer> findByUsername(String username) {
        return Optional.ofNullable(customers.get(username));
    }
}
