package dev.lumas.shops.interfaces;

import lombok.NonNull;

/**
 * Represents an enum type.
 * @param <E> The enum type.
 */
public interface EnumType<E extends Enum<E>> extends Accessor<@NonNull Object> {
    /**
     * Gets the enum type.
     * @return The enum type.
     */
    E type();
}
