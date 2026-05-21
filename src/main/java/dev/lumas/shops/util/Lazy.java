package dev.lumas.shops.util;

import org.jspecify.annotations.Nullable;

import java.util.function.Supplier;

public final class Lazy<T> implements Supplier<T> {
    private @Nullable Supplier<T> supplier;
    private @Nullable T value;

    public static <T> Lazy<T> of(Supplier<T> supplier) {
        return new Lazy<>(supplier);
    }

    private Lazy(@Nullable Supplier<T> supplier) {
        this.supplier = supplier;
    }

    @Override
    public T get() {
        if (supplier != null) {
            value = supplier.get();
            supplier = null;  // release the closure so it can be GC'd
        }
        return value;
    }
}