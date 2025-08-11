package io.github.etr.playground.domain;

import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductCatalog productCatalog;
    private final CustomerRelationshipManagement crm;

    public Order createOrder(String username, List<ProductSku> productSkus) {
        Customer customer = crm.findByUsernameOrElseThrow(username);

        List<Product> products = productSkus.stream()
            .map(productCatalog::findBySkuOrElseThrow)
            .toList();

        Order order = new Order(customer, products);
        order = orderRepository.save(order);
        return order;
    }

}
