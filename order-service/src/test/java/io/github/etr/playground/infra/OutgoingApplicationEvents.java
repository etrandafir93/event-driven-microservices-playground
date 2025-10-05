package io.github.etr.playground.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import io.github.etr.playground.domain.receipt.ReceiptReady;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class OutgoingApplicationEvents {

    private final Map<Class<?>, List<Object>> events = new ConcurrentHashMap<>();

    @EventListener
    public void receiptReadyEvents(ReceiptReady event) {
        log.info("Received application event: {}", event);
        events.computeIfAbsent(ReceiptReady.class, __ -> new ArrayList<>())
            .add(event);
    }

    public <T> T awaitForEvent(Class<T> eventClass) {
        await().until(() -> events.containsKey(eventClass) && !events.get(eventClass).isEmpty());

        List<Object> eventList = events.get(eventClass);
        assertThat(eventList).hasSize(1);

        return eventClass.cast(eventList.getFirst());
    }

    public void clear() {
        events.clear();
    }

}