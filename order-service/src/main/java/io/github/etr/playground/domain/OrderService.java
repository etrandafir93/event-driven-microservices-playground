package io.github.etr.playground.domain;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;

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

    public List<Order> userOrders(String username) {
        return orderRepository.findByUsername(username);
    }
}
