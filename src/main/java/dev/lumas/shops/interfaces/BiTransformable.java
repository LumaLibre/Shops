package dev.lumas.shops.interfaces;

public interface BiTransformable<T> extends Transformable<T> {
    void accept(T value);
}
