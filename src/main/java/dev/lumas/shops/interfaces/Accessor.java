package dev.lumas.shops.interfaces;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public interface Accessor<T> {
    @Nullable T get();
}
