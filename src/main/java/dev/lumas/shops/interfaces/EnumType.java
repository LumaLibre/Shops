package dev.lumas.shops.interfaces;

import lombok.NonNull;

public interface EnumType<E extends Enum<E>> extends Accessor<@NonNull Object> {
    E type();
}
