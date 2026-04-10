package io.github.etr.demo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
class OrderController {

	private final JPrimeOrderService orderService;

	@PostMapping
	ResponseEntity<?> createOrder(@RequestBody Map<String, String> request) {
		String customerId = request.get("customerId");
		String customerEmail = request.get("customerEmail");

		log.info("Creating order for customer: {}", customerId);
		Order order = buildDummyOrder(customerId);

		orderService.createOrder(order, customerId, customerEmail);

		return ResponseEntity.ok(
				Map.of("success", true, "orderNumber", order.getOrderNumber()));
	}

	private static Order buildDummyOrder(String customerId) {
		String orderNumber =
				"ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
		Order order1 = Order.builder().orderNumber(orderNumber).customerId(customerId)
				.orderDate(LocalDateTime.now()).status(Order.OrderStatus.PENDING)
				.totalAmount(BigDecimal.ZERO).build();

		OrderLine line1 = OrderLine.builder().productId("PROD-001").productName("Laptop")
				.quantity(1).unitPrice(new BigDecimal("999.99"))
				.totalPrice(new BigDecimal("999.99")).build();

		OrderLine line2 = OrderLine.builder().productId("PROD-002").productName("Mouse")
				.quantity(2).unitPrice(new BigDecimal("29.99"))
				.totalPrice(new BigDecimal("59.98")).build();

		order1.addOrderLine(line1);
		order1.addOrderLine(line2);
		order1.setTotalAmount(line1.getTotalPrice().add(line2.getTotalPrice()));
		return order1;
	}
}
