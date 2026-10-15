package com.technova.generics.model;

import java.util.Objects;
import java.util.Optional;

/**
 * A generic container restricted to numeric types using an upper bounded type parameter.
 *
 * The bound {@code <T extends Number>} guarantees at compile time that any type
 * stored in this box is an instance of {@link Number} (such as Integer, Double, Long, Float),
 * enabling direct access to methods like {@link Number#doubleValue()} without casting.
 *
 * Types such as {@code String} or {@code Product} are rejected at compile time.
 *
 * @param <T> a subtype of {@link Number}
 */
public class Box<T extends Number> {

    private T value;

    /**
     * Constructs an empty Box.
     */
    public Box() {
        this.value = null;
    }

    /**
     * Constructs a Box holding the specified numeric value.
     */
    public Box(T value) {
        this.value = value;
    }

    /**
     * Static factory method to instantiate a Box.
     */
    public static <T extends Number> Box<T> of(T value) {
        return new Box<>(value);
    }

    /**
     * Static factory method to instantiate an empty Box.
     */
    public static <T extends Number> Box<T> empty() {
        return new Box<>();
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    /**
     * Identifies whether a numeric value is present in this Box.
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Returns the stored value as an {@link Optional}.
     */
    public Optional<T> getOptional() {
        return Optional.ofNullable(value);
    }

    /**
     * Converts the stored numeric value to a primitive double.
     *
     * Because T extends Number, {@link Number#doubleValue()} is guaranteed to be available.
     *
     * @return double representation of the value
     * @throws IllegalStateException if the Box is empty (contains null)
     */
    public double doubleValue() {
        if (value == null) {
            throw new IllegalStateException("Cannot retrieve doubleValue from an empty Box");
        }
        return value.doubleValue();
    }

    /**
     * Returns the stored value as a double, or the specified default if empty.
     */
    public double doubleValueOrDefault(double defaultValue) {
        return (value != null) ? value.doubleValue() : defaultValue;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Box<?> box)) return false;
        return Objects.equals(value, box.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return isPresent() ? String.format("Box[%s (%s)]", value, value.getClass().getSimpleName()) : "Box[EMPTY]";
    }
}
