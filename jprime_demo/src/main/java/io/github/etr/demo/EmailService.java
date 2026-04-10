package io.github.etr.demo;

interface EmailService {
    void sendOrderConfirmation(String customerEmail, String orderNumber, String orderDetails);
}
