package io.github.etr.demo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
class FakeEmailService implements EmailService {

    @Override
    public void sendOrderConfirmation(String customerEmail, String orderNumber, String orderDetails) {
        log.info("Sending email to: {} for order: {}", customerEmail, orderNumber);

        // TODO: Integrate with SendGrid, AWS SES, etc.

        // Simulate random failures (20% failure rate)
        if (Math.random() < 0.2) {
            throw new RuntimeException("Email service down!");
        }
    }
}
