package io.github.etr.playground.domain.order;

import static java.util.stream.Collectors.toMap;
import static net.logstash.logback.argument.StructuredArguments.kv;

import java.util.Map;
import java.util.NoSuchElementException;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductCatalog productCatalog;
    private final CustomerRelationshipManagement crm;
    private final ApplicationEventPublisher applicationEvents;

    @Transactional
    public Order createOrder(String username, Map<ProductSku, Integer> quantityBySku) {
        log.info("Received create order request for username: {}, products: {}",  kv("user", username), quantityBySku);

        Customer customer = crm.findByUsernameOrElseThrow(username);
        var quantityByProduct = quantityBySku.entrySet()
            .stream()
            .collect(toMap(
                entry -> productCatalog.findBySkuOrElseThrow(entry.getKey()),
                Map.Entry::getValue));

        Order order = new Order(customer, quantityByProduct);
        order = orderRepository.save(order);

        log.info("Created order for {}, order: {}", kv("user", username), order);
        applicationEvents.publishEvent(OrderCreatedEvent.from(order));
        return order;
    }

    @Transactional
    @EventListener
    public void orderShipped(OrderShippedEvent event) {
        String username = event.username();
        String orderId = event.orderId();
        log.info("received OrderShippedEvent {} for {}", event, kv("user", username));

        Order order = orderRepository.findByOrderId(orderId)
            .filter(it -> it.customerUsername().equals(username))
            .orElseThrow(() -> new NoSuchElementException("Order not found for orderId: %s and username: %s".formatted(orderId, username)));

        order.shipped();
        orderRepository.save(order);
    }

    @Transactional
    @EventListener
    public void orderDelivered(OrderDeliveredEvent event) {
        String username = event.username();
        String orderId = event.orderId();
        log.info("received OrderDeliveredEvent {} for {}", event, kv("user", username));

        Order order = orderRepository.findByOrderId(orderId)
            .filter(it -> it.customerUsername().equals(username))
            .orElseThrow(() -> new NoSuchElementException("Order not found for orderId: %s and username: %s".formatted(orderId, username)));

        order.delivered();
        orderRepository.save(order);
    }
}
