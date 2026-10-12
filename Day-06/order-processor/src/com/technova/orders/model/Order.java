package com.technova.orders.model;

import java.time.LocalDateTime;

/**
 * Domain model representing a customer order in the inventory processing system.
 */
public class Order {
    private static int orderCounter = 1000;

    private final String orderId;
    private final String product;
    private final int quantity;
    private String status;
    private final LocalDateTime createdAt;

    public Order(String product, int quantity) {
        this("ORD-" + (++orderCounter), product, quantity);
    }

    public Order(String orderId, String product, int quantity) {
        this.orderId = orderId;
        this.product = product;
        this.quantity = quantity;
        this.status = "PENDING";
        this.createdAt = LocalDateTime.now();
    }

    public static synchronized void resetCounter(int base) {
        orderCounter = base;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("Order[id='%s', product='%s', qty=%d, status='%s', time=%s]",
                orderId, product, quantity, status, createdAt);
    }
}
