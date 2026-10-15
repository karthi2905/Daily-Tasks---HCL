package com.technova.generics.model;

import java.util.Objects;

/**
 * An immutable generic tuple storing a typed key and a typed value.
 *
 * Demonstrates:
 * - Type parameters K and V providing compile-time type safety.
 * - Elimination of explicit object casting.
 * - Complete immutability and consistent object contracts.
 *
 * @param <K> the type of the key
 * @param <V> the type of the value
 */
public class Pair<K, V> {

    private final K key;
    private final V value;

    /**
     * Constructs a Pair with the specified key and value.
     */
    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Static factory method for concise instantiation without raw types.
     */
    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<>(key, value);
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair<?, ?> pair)) return false;
        return Objects.equals(key, pair.key) && Objects.equals(value, pair.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        return String.format("Pair(%s -> %s)", key, value);
    }
}
