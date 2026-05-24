package dev.lumas.shops.interfaces;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Represents a getter that can return null.
 * @param <T> The type of the value that can be returned.
 */
@NullMarked
public interface Accessor<T> {

    /**
     * Gets the value.
     * @return The value.
     */
    @Nullable T get();
}
