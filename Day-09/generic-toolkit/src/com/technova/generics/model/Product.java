package com.technova.generics.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a retail product with ID, name, category, and price.
 *
 * Implements {@link Comparable} based on price (with ID tie-breaker)
 * to demonstrate generic algorithms like {@code max()} operating on custom domain models.
 */
public class Product implements Comparable<Product> {

    private final int id;
    private final String name;
    private final String category;
    private final BigDecimal price;

    public Product(int id, String name, String category, BigDecimal price) {
        if (id <= 0) {
            throw new IllegalArgumentException(String.format("Product ID must be strictly positive (> 0), got: %d", id));
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or blank");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new IllegalArgumentException("Product category cannot be null or blank");
        }
        if (price == null) {
            throw new IllegalArgumentException("Product price cannot be null");
        }
        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(String.format("Product price cannot be negative, got: %s", price));
        }

        this.id = id;
        this.name = name.trim();
        this.category = category.trim();
        this.price = price;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getPrice() {
        return price;
    }

    /**
     * Compares products primarily by price ascending, breaking ties by ID.
     */
    @Override
    public int compareTo(Product other) {
        Objects.requireNonNull(other, "Cannot compare to null Product");
        int priceComp = this.price.compareTo(other.price);
        if (priceComp != 0) {
            return priceComp;
        }
        return Integer.compare(this.id, other.id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product product)) return false;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    @Override
    public String toString() {
        return String.format("[ID: %03d] %-18s | Cat: %-12s | Price: $%,9.2f",
                id, name, category, price);
    }
}
