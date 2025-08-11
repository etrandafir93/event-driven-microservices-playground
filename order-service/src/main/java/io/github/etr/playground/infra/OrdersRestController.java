package io.github.etr.playground.infra;

import java.util.List;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.etr.playground.domain.Order;
import io.github.etr.playground.domain.OrderService;
import io.github.etr.playground.domain.ProductSku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
class OrdersRestController {

    private final OrderService orderService;

    @PostMapping
    public CreateOrderResponse createOrder(@RequestBody CreateOrderRequest request) {
        Order createdOrder = orderService.createOrder(request.username(), request.productSkus());
        return new CreateOrderResponse(createdOrder.getOrderId(), createdOrder.getStatus()
            .getDescription());
    }

    record CreateOrderRequest(String username, List<String> products) {
        List<ProductSku> productSkus() {
            return products.stream()
                .map(ProductSku::new)
                .toList();
        }
    }

    record CreateOrderResponse(String orderId, String status) {
    }

}
