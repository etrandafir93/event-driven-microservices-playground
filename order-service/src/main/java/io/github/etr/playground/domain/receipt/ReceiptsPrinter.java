package io.github.etr.playground.domain.receipt;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import io.github.etr.playground.application.SystemTime;
import io.github.etr.playground.domain.order.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
class ReceiptsPrinter {

    private final SystemTime systemTime;
    private final ApplicationEventPublisher applicationEvents;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    void onOrderPlaced(OrderCreatedEvent order) {
        Receipt receipt = toReceipt(order);
        // more logic here
        log.info("Receipt created for order {}", order.orderId());
        applicationEvents.publishEvent(new ReceiptReady(receipt));
    }

    private Receipt toReceipt(OrderCreatedEvent order) {
        return order.order()
            .entrySet()
            .stream()
            .map(e -> new Receipt.Item(e.getKey(), e.getValue()))
            .sorted(comparing(Receipt.Item::sku))
            .collect(collectingAndThen(toList(),
                items -> new Receipt(systemTime.now(), order.orderId(), order.username(), items))
            );
    }

}
