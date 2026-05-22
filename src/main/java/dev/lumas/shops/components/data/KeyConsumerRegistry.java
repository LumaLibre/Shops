package dev.lumas.shops.components.data;

import io.papermc.paper.dialog.DialogResponseView;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maps incoming {@link Key} identifiers from client round-trips (dialog clicks,
 * custom click events) back to the original {@link KeyConsumer} that should
 * handle them.
 *
 * <p>Per-player so two players can have different bound parents for the same
 * identifier (e.g. each viewing their own ConfirmationDialog).
 *
 * <p>Clicking any registered key dismisses all sibling handlers for that player
 * — confirmation dialogs are one-shot.
 */
@NullMarked
public final class KeyConsumerRegistry {

    public static final KeyConsumerRegistry INSTANCE = new KeyConsumerRegistry();

    private final Map<UUID, Map<Key, KeyConsumer<?>>> pending = new HashMap<>();

    public void register(Player player, KeyConsumer<?>... handlers) {
        Map<Key, KeyConsumer<?>> map = pending.computeIfAbsent(player.getUniqueId(), id -> new HashMap<>());
        for (KeyConsumer<?> h : handlers) {
            map.put(h.key(), h);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    public boolean dispatch(Player player, Key key, DialogResponseView view) {
        Map<Key, KeyConsumer<?>> map = pending.get(player.getUniqueId());
        if (map == null) return false;
        KeyConsumer<?> handler = map.get(key);
        if (handler == null) return false;
        pending.remove(player.getUniqueId());
        handler.call(player, view);
        return true;
    }

    public void clear(Player player) {
        pending.remove(player.getUniqueId());
    }
}