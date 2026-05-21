package dev.lumas.shops.components.data;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@NullMarked
public record KeyConsumer<T>(Key key, @Nullable T parent, BiConsumer<@Nullable T, Player> consumer) implements Keyed {

    public static <T> KeyConsumer<T> of(Key key, BiConsumer<T, Player> consumer) {
        return new KeyConsumer<>(key, null, consumer);
    }

    public static KeyConsumer<Void> of(Key key, Runnable runnable) {
        return new KeyConsumer<>(key, null, (_, _) -> runnable.run());
    }

    public KeyConsumer<T> withParent(T parent) {
        return new KeyConsumer<>(key, parent, consumer);
    }

    public void call(Player player) {
        consumer.accept(parent, player);
    }

}