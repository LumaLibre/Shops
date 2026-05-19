package dev.lumas.shops.components.data;

import java.util.function.Function;

public record TypedEntry<T, R>(Class<T> type, Function<T, R> factory) {
    public R create(Object value) {
        return factory.apply(type.cast(value));
    }
}