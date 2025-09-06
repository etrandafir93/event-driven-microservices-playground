package io.github.etr.playground.domain.loyalty;

import java.util.Optional;

@FunctionalInterface
public interface TieredMembers {

    Optional<Member> findByUsername(String username);

    record Member(String username, int points, String tier) {
    }
}
