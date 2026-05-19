package dev.lumas.shops.interfaces;

public interface Accessor<T> {
    T get();
    void set(T value);
}
