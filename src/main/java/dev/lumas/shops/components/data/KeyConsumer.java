package dev.lumas.shops.components.data;

import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public record KeyConsumer<T>(T parent, Key key, Handler<T> consumer) implements Keyed {

    @FunctionalInterface
    public interface Handler<T> {
        void handle(T parent, Player player, DialogResponseView view);
    }

    public static <T> KeyConsumer<T> of(T parent, Key key, Handler<T> consumer) {
        return new KeyConsumer<>(parent, key, consumer);
    }

    public void call(Player player, DialogResponseView view) {
        consumer.handle(parent, player, view);
    }
}