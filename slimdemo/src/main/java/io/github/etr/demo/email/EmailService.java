package io.github.etr.demo.email;

public interface EmailService {
    void sendOrderConfirmation(String customerEmail, String orderNumber, String orderDetails);
}
