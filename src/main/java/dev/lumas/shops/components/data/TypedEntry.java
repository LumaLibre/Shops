package dev.lumas.shops.components.data;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

@NullMarked
public record TypedEntry<T, R>(Class<T> type, Function<@Nullable T, R> factory) {
    public R create(@Nullable Object value) {
        T cast = value == null ? null : type.cast(value);
        return factory.apply(cast);
    }
}