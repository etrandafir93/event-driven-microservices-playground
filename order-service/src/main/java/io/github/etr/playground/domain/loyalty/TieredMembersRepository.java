package io.github.etr.playground.domain.loyalty;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TieredMembersRepository extends JpaRepository<TieredMember, Long> {
    Optional<TieredMember> findByUsername(String username);
}
