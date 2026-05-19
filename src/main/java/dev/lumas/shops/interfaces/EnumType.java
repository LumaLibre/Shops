package dev.lumas.shops.interfaces;

public interface EnumType<E extends Enum<E>> extends Accessor<Object> {
    E type();
}
