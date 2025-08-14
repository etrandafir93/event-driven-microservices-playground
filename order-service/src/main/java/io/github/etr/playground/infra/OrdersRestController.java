package io.github.etr.playground.infra;

import static java.util.stream.Collectors.toMap;

import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

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
    CreateOrderResponse createOrder(@RequestBody @Valid CreateOrderRequest request) {
        Order createdOrder = orderService.createOrder(request.username(), request.quantityByProductSku());
        return new CreateOrderResponse(
            createdOrder.orderId(),
            createdOrder.status().description());
    }

    record CreateOrderRequest(
        @NotBlank String username,
        Map<@NotBlank String, @NotNull @Min(1) @Max(100) Integer> products
    ) {
        public Map<ProductSku, Integer> quantityByProductSku() {
            return products().entrySet()
                .stream()
                .collect(toMap(
                    entry -> new ProductSku(entry.getKey()),
                    Map.Entry::getValue)
                );
        }
    }

    record CreateOrderResponse(String orderId, String status) {
    }

}
