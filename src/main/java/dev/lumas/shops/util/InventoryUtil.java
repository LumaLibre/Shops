package dev.lumas.shops.util;

import dev.lumas.shops.interfaces.ShopsInventory;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.function.BiConsumer;

@NullMarked
@UtilityClass
public final class InventoryUtil {

    public static void closeAllMarkets() {
        closeAllMarkets(null, true);
    }

    public static void closeAllMarkets(boolean schedule) {
        closeAllMarkets(null, schedule);
    }

    public static void closeAllMarkets(@Nullable Key key) {
        closeAllMarkets(key, true);
    }

    public static void closeAllMarkets(@Nullable Key key, boolean schedule) {
        iterateOpen(schedule, (player, inventory) -> {
            if (inventory.getHolder(false) instanceof ShopsInventory inv && (key == null || key.equals(inv.key()))) {
                player.closeInventory();
            }
        });
    }

    public static void iterateOpen(boolean schedule, BiConsumer<Player, Inventory> consumer) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (schedule) {
                Scheduling.entity(player, () -> consumer.accept(player, player.getOpenInventory().getTopInventory()));
            } else {
                consumer.accept(player, player.getOpenInventory().getTopInventory());
            }
        }
    }
}
