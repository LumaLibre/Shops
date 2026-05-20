package dev.lumas.shops.components.data;

import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NullMarked;

import java.util.function.Consumer;

@NullMarked
public record KeyConsumer<T>(Key key, Consumer<T> consumer) implements Key {

    public void call(T t) {
        consumer.accept(t);
    }

    public static <T> KeyConsumer<T> of(Key key, Consumer<T> consumer) {
        return new KeyConsumer<>(key, consumer);
    }


    @Override
    @KeyPattern.Namespace
    public String namespace() {
        return key.namespace();
    }

    @Override
    @KeyPattern.Value
    public String value() {
        return key.value();
    }

    @Override
    public String asString() {
        return key.asString();
    }
}
