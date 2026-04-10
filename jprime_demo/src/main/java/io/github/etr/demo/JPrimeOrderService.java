package io.github.etr.demo;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
class JPrimeOrderService {

	private final OrderRepository orderRepository;
	private final ProductRepository productRepository;
	private final KafkaTemplate<String, String> messageSender;
	private final LoyaltyServiceClient loyaltyServiceClientV2;
	private final EmailService emailService;

	public void createOrder(Order order, String customerId, String customerEmail) {
		order = orderRepository.save(order);
		log.info("Order saved to DB: {}", order.getOrderNumber());

		for (OrderLine line : order.getOrderLines()) {
			Product product = productRepository.findById(line.getProductId()).orElseThrow(
					() -> new RuntimeException("Product not found: " + line.getProductId()));
			product.reduceStock(line.getQuantity());
			productRepository.save(product);
		}
		log.info("Product stock reduced");

		publishOrderCreatedEvent(order);
		log.info("event published to 'order-events' topic");

		saveOrderBackupToDisk(order);
		log.info("File backup created");

		loyaltyServiceClientV2.postAwardPoints(customerId, order.getOrderNumber(),
				order.getTotalAmount().doubleValue());
		log.info("POST request send to Loyalty Service");

		String orderDetails = buildOrderDetailsForEmail(order);
		emailService.sendOrderConfirmation(customerEmail, order.getOrderNumber(),
				orderDetails);
		log.info("Email sent to customer: {}", customerEmail);
	}

	private void publishOrderCreatedEvent(Order order) {
		var msg = Map.of("eventType", "OrderCreated", "orderNumber",
				order.getOrderNumber(), "customerId", order.getCustomerId(),
				"totalAmount", order.getTotalAmount(), "orderDate",
				order.getOrderDate().toString());
		messageSender.send("order-events", order.getOrderNumber(), toJson(msg));
	}

	private void saveOrderBackupToDisk(Order order) {
		Path backupDir = Paths.get("order-backups");
		createDirectory(backupDir);

		String fileName = "order-" + order.getOrderNumber() + ".json";
		Path filePath = backupDir.resolve(fileName);

		Map<String, Object> orderData = new HashMap<>();
		orderData.put("orderNumber", order.getOrderNumber());
		orderData.put("customerId", order.getCustomerId());
		orderData.put("totalAmount", order.getTotalAmount());
		orderData.put("orderDate", order.getOrderDate().toString());
		orderData.put("status", order.getStatus());

		String json = toJson(orderData);
		writeToFile(filePath, json);
	}

	private String buildOrderDetailsForEmail(Order order) {
		StringBuilder sb = new StringBuilder();
		sb.append("Order Number: ").append(order.getOrderNumber()).append("\n");
		sb.append("Order Date: ").append(order.getOrderDate()).append("\n");
		sb.append("Items:\n");
		for (OrderLine line : order.getOrderLines()) {
			sb.append("  - ").append(line.getProductName()).append(" x")
					.append(line.getQuantity()).append(" @ $").append(line.getUnitPrice())
					.append("\n");
		}
		sb.append("Total: $").append(order.getTotalAmount());
		return sb.toString();
	}

	private String toJson(Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		}
		catch (Exception e) {
			throw new RuntimeException("JSON serialization failed", e);
		}
	}

	private void createDirectory(Path path) {
		try {
			Files.createDirectories(path);
		}
		catch (IOException e) {
			throw new RuntimeException("Directory creation failed", e);
		}
	}

	private void writeToFile(Path path, String content) {
		try (FileWriter writer = new FileWriter(path.toFile())) {
			writer.write(content);
		}
		catch (IOException e) {
			throw new RuntimeException("File write failed", e);
		}
	}
}
