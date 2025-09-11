package io.github.etr.playground.domain.loyalty;

import java.util.Map;
import java.util.Optional;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import io.github.etr.playground.application.SystemTime;
import io.github.etr.playground.domain.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
class TieredMembershipService implements TieredMembers {

    private final TieredMembersRepository membersRepo;
    private final SystemTime systemTime;

    @Override
    public Optional<Member> findByUsername(String username) {
        return membersRepo.findByUsername(username)
            .map(it -> new Member(it.username(), it.points(), it.tier().name()));
    }

    @EventListener
    void onOrder(OrderCreatedEvent event) {
        log.info("Processing order {} for user {}", event.order(), event.username());
        String username = event.username();

        TieredMember member = membersRepo.findByUsername(username)
            .orElseGet(() -> new TieredMember(username));

        int newPoints = pointsToEarn(event.order());
        member.earn(newPoints);

        membersRepo.save(member);
    }

    int pointsToEarn(Map<String, Integer> order) {
        return order.values()
            .stream()
            .mapToInt(it -> it * pointsMultiplier())
            .sum();
    }

    private int pointsMultiplier() {
        return switch (systemTime.now().getDayOfWeek()) {
            case MONDAY, TUESDAY -> 10;
            case WEDNESDAY -> 20;
            case THURSDAY,FRIDAY -> 30 ;
            case SATURDAY, SUNDAY -> 50;
        };
    }

}

