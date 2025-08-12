package io.github.etr.playground.domain;

import static java.util.stream.Collectors.toMap;

import java.util.Map;
import java.util.function.Function;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        applicationEvents.publishEvent(new OrderCreatedEvent(order));
        return order;
    }

}
