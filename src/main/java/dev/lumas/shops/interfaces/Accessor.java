package dev.lumas.shops.interfaces;

public interface Accessor<T> {
    T get();
    default void set(T value) {}
}
