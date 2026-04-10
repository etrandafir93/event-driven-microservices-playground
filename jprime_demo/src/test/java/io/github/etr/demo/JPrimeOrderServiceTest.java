package io.github.etr.demo;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class JPrimeOrderServiceTest {

	@Mock
	private OrderRepository orderRepository;
	@Mock
	private ProductRepository productRepository;
	@Mock
	private KafkaTemplate<String, String> messageSender;
	@Mock
	private LoyaltyServiceClient loyaltyServiceClientV2;
	@Mock
	private EmailService emailService;

	@InjectMocks
	private JPrimeOrderService orderService;

	@Test
	void testCreateOrder_Success() {
		// given
		Order savedOrder = anOrder();
		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
		when(productRepository.findById("PROD-001")).thenReturn(Optional.of(product1));
		when(productRepository.findById("PROD-002")).thenReturn(Optional.of(product2));
		when(productRepository.save(product1)).thenReturn(product1);
		when(productRepository.save(product2)).thenReturn(product2);

		// when
		orderService.createOrder(testOrder, customerId, customerEmail);

		// then
		verify(orderRepository, times(1)).save(any(Order.class));
		verify(productRepository, times(1)).findById("PROD-001");
		verify(productRepository, times(1)).findById("PROD-002");
		assertEquals(48, product1.getStockQuantity());
		assertEquals(97, product2.getStockQuantity());
		verify(productRepository, times(1)).save(product1);
		verify(productRepository, times(1)).save(product2);
		verify(messageSender, times(1)).send(eq("order-events"), eq("ORD-2024-001"), contains("OrderCreated"));
		verify(loyaltyServiceClientV2, times(1)).postAwardPoints(eq(customerId), eq("ORD-2024-001"), eq(new BigDecimal("2089.95")));
		verify(emailService, times(1)).sendOrderConfirmation(eq(customerEmail), eq("ORD-2024-001"), contains("Order Number: ORD-2024-001"));
		verifyNoMoreInteractions(orderRepository);
		verifyNoMoreInteractions(productRepository);
		verifyNoMoreInteractions(messageSender);
		verifyNoMoreInteractions(loyaltyServiceClientV2);
		verifyNoMoreInteractions(emailService);
	}




	private Order testOrder;
	private OrderLine orderLine1;
	private OrderLine orderLine2;
	private Product product1;
	private Product product2;
	private String customerId;
	private String customerEmail;

	@BeforeEach
	void setUp() {
		customerId = "CUST-12345";
		customerEmail = "customer@example.com";

		product1 = Product.builder()
				.productId("PROD-001")
				.name("Laptop")
				.stockQuantity(50)
				.build();

		product2 = Product.builder()
				.productId("PROD-002")
				.name("Mouse")
				.stockQuantity(100)
				.build();

		orderLine1 = OrderLine.builder()
				.id(1L)
				.productId("PROD-001")
				.productName("Laptop")
				.quantity(2)
				.unitPrice(new BigDecimal("999.99"))
				.totalPrice(new BigDecimal("1999.98"))
				.build();

		orderLine2 = OrderLine.builder()
				.id(2L)
				.productId("PROD-002")
				.productName("Mouse")
				.quantity(3)
				.unitPrice(new BigDecimal("29.99"))
				.totalPrice(new BigDecimal("89.97"))
				.build();

		List<OrderLine> orderLines = new ArrayList<>();
		orderLines.add(orderLine1);
		orderLines.add(orderLine2);

		testOrder = Order.builder()
				.id(null)
				.orderNumber("ORD-2024-001")
				.customerId(customerId)
				.totalAmount(new BigDecimal("2089.95"))
				.orderDate(LocalDateTime.of(2024, 1, 15, 10, 30))
				.status(Order.OrderStatus.PENDING)
				.orderLines(orderLines)
				.build();

		orderLine1.setOrder(testOrder);
		orderLine2.setOrder(testOrder);
	}

	private Order anOrder() {
		return Order.builder().id(100L).orderNumber(testOrder.getOrderNumber())
				.customerId(testOrder.getCustomerId())
				.totalAmount(testOrder.getTotalAmount())
				.orderDate(testOrder.getOrderDate()).status(testOrder.getStatus())
				.orderLines(testOrder.getOrderLines()).build();
	}

	@Test
	@DisplayName("Should throw exception when product not found")
	void testCreateOrder_ProductNotFound() {
		var order = anOrder();
		when(orderRepository.save(any(Order.class))).thenReturn(order);
		when(productRepository.findById("PROD-001")).thenReturn(Optional.empty());

		assertThrows(RuntimeException.class, () -> {
			orderService.createOrder(testOrder, customerId, customerEmail);
		});

		verify(orderRepository, times(1)).save(any(Order.class));
		verify(productRepository, times(1)).findById("PROD-001");
		verify(productRepository, never()).save(any(Product.class));
		verify(messageSender, never()).send(anyString(), anyString(), anyString());
		verify(loyaltyServiceClientV2, never()).postAwardPoints(anyString(), anyString(), any(BigDecimal.class));
		verify(emailService, never()).sendOrderConfirmation(anyString(), anyString(), anyString());
	}

	@Test
	@DisplayName("Should handle single order line correctly")
	void testCreateOrder_SingleOrderLine() {
		List<OrderLine> singleLineList = new ArrayList<>();
		singleLineList.add(orderLine1);

		Order singleLineOrder = Order.builder()
				.orderNumber("ORD-2024-002")
				.customerId(customerId)
				.totalAmount(new BigDecimal("1999.98"))
				.orderDate(LocalDateTime.now())
				.status(Order.OrderStatus.PENDING)
				.orderLines(singleLineList)
				.build();
		orderLine1.setOrder(singleLineOrder);

		Order savedOrder = Order.builder()
				.id(200L)
				.orderNumber(singleLineOrder.getOrderNumber())
				.customerId(singleLineOrder.getCustomerId())
				.totalAmount(singleLineOrder.getTotalAmount())
				.orderDate(singleLineOrder.getOrderDate())
				.status(singleLineOrder.getStatus())
				.orderLines(singleLineOrder.getOrderLines())
				.build();

		when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
		when(productRepository.findById("PROD-001")).thenReturn(Optional.of(product1));
		when(productRepository.save(product1)).thenReturn(product1);

		orderService.createOrder(singleLineOrder, customerId, customerEmail);

		verify(productRepository, times(1)).findById("PROD-001");
		verify(productRepository, never()).findById("PROD-002");
		verify(productRepository, times(1)).save(product1);
		assertEquals(48, product1.getStockQuantity());
		assertEquals(100, product2.getStockQuantity());
	}
}