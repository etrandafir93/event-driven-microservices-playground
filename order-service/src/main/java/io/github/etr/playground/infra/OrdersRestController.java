package io.github.etr.playground.infra;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import io.github.etr.playground.domain.order.Order;
import io.github.etr.playground.domain.order.OrderRepository;
import io.github.etr.playground.domain.order.OrderService;
import io.github.etr.playground.domain.order.ProductSku;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management API - Create and retrieve customer orders")
class OrdersRestController {

    private final OrderService orderService;
    private final OrderRepository orderRepo;

    @PostMapping
    @Operation(
        summary = "Create a new order",
        description = "Creates a new order for a customer with specified products and quantities",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Order created successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderView.class)
                )
            ),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
        }
    )
    OrderView createOrder(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Order creation request",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = CreateOrderRequest.class),
                examples = @ExampleObject(
                    name = "Sample Order",
                    value = """
                        {
                          "username": "john_doe",
                          "products": {
                            "TV-55-SAM-QLED": 2,
                            "LAPTOP-DELL-XPS": 1,
                            "PHONE-IPHONE-15": 3
                          }
                        }
                        """
                )
            )
        )
        @RequestBody @Valid CreateOrderRequest request
    ) {
        Order createdOrder = orderService.createOrder(request.username(), quantityByProductSku(request));
        return mapOrder(createdOrder);
    }

    @GetMapping
    @Operation(
        summary = "Get all orders for a user",
        description = "Retrieves all orders for a specific username",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Orders retrieved successfully",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetOrdersResponse.class),
                    examples = @ExampleObject(
                        name = "Sample Response",
                        value = """
                            {
                              "username": "john_doe",
                              "orders": [
                                {
                                  "orderId": "ORD-123456",
                                  "totalValue": 2999.99,
                                  "status": "PENDING",
                                  "statusDescription": "Order is pending processing",
                                  "lastUpdatedAt": "2025-11-12T10:30:00"
                                }
                              ]
                            }
                            """
                    )
                )
            )
        }
    )
    GetOrdersResponse gerOrders(
        @Parameter(
            description = "Username to retrieve orders for",
            required = true,
            example = "john_doe"
        )
        @RequestParam String username
    ) {
        return orderRepo.findByUsername(username)
            .stream()
            .map(this::mapOrder)
            .collect(collectingAndThen(
                Collectors.toList(),
                orders -> new GetOrdersResponse(username, orders)));
    }

    @GetMapping("/{orderId}")
    @Operation(
        summary = "Get a specific order by ID",
        description = "Retrieves a single order by its order ID",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Order found",
                content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderView.class),
                    examples = @ExampleObject(
                        name = "Sample Order",
                        value = """
                            {
                              "orderId": "ORD-123456",
                              "totalValue": 2999.99,
                              "status": "PENDING",
                              "statusDescription": "Order is pending processing",
                              "lastUpdatedAt": "2025-11-12T10:30:00"
                            }
                            """
                    )
                )
            ),
            @ApiResponse(responseCode = "404", description = "Order not found")
        }
    )
    ResponseEntity<OrderView> gerOrder(
        @Parameter(
            description = "Order ID to retrieve",
            required = true,
            example = "ORD-123456"
        )
        @PathVariable String orderId
    ) {
        var resp =  orderRepo.findByOrderId(orderId)
            .map(this::mapOrder);
        return ResponseEntity.of(resp);
    }

    private static Map<ProductSku, Integer> quantityByProductSku(CreateOrderRequest req) {
        return req.products().entrySet()
            .stream()
            .collect(toMap(
                entry -> new ProductSku(entry.getKey()),
                Map.Entry::getValue));
    }

    private OrderView mapOrder(Order it) {
        return new OrderView(it.orderId(), it.totalValue(), it.status()
            .name(), it.status()
            .description(), it.updatedAt());
    }

    @Schema(description = "Request to create a new order")
    record CreateOrderRequest(
        @Schema(description = "Username of the customer", example = "john_doe")
        @NotBlank String username,

        @Schema(
            description = "Map of product SKUs to quantities (1-100)",
            example = "{\"TV-55-SAM-QLED\": 2, \"LAPTOP-DELL-XPS\": 1}"
        )
        Map<@NotBlank String, @NotNull @Min(1) @Max(100) Integer> products
    ) {
    }

    @Schema(description = "Response containing all orders for a user")
    record GetOrdersResponse(
        @Schema(description = "Username", example = "john_doe")
        String username,

        @Schema(description = "List of orders")
        List<OrderView> orders
    ) {
    }

    @Schema(description = "Order details")
    record OrderView(
        @Schema(description = "Unique order identifier", example = "ORD-123456")
        String orderId,

        @Schema(description = "Total order value", example = "2999.99")
        BigDecimal totalValue,

        @Schema(description = "Order status", example = "PENDING")
        String status,

        @Schema(description = "Human-readable status description", example = "Order is pending processing")
        String statusDescription,

        @Schema(description = "Timestamp of last update", example = "2025-11-12T10:30:00")
        LocalDateTime lastUpdatedAt
    ) {
    }

}
