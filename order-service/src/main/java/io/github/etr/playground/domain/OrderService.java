package io.github.etr.playground.domain;

import static java.util.stream.Collectors.toMap;

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
    public Order createOrder(String username, Map<ProductSku, Integer> skuQuantities) {
        Map<Product, Integer> productQuantities = skuQuantities.entrySet()
            .stream()
            .collect(toMap(
                entry -> productCatalog.findBySkuOrElseThrow(entry.getKey()),
                Map.Entry::getValue));

        Customer customer = crm.findByUsernameOrElseThrow(username);

        Order order = new Order(customer, productQuantities);
        order = orderRepository.save(order);

        applicationEvents.publishEvent(OrderCreatedEvent.from(order));
        return order;
    }

    @Transactional
    @EventListener
    public void orderShipped(OrderShippedEvent event) {
        log.info("received OrderShippedEvent {}", event);
        String username = event.username();
        String orderId = event.orderId();

        Order order = orderRepository.findByOrderId(orderId)
            .filter(it -> it.customerUsername().equals(username))
            .orElseThrow(() -> new NoSuchElementException("Order not found for orderId: %s and username: %s".formatted(orderId, username)));

        order.shipped();
        orderRepository.save(order);
    }
}
