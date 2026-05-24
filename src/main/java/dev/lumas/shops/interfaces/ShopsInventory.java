package dev.lumas.shops.interfaces;

import dev.lumas.shops.util.Scheduling;
import net.kyori.adventure.key.Keyed;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;

/**
 * Represents an inventory used by this plugin.
 */
@NullMarked
public interface ShopsInventory extends InventoryHolder, Keyed {

    default void open(Player player) {
        if (Bukkit.isOwnedByCurrentRegion(player)) {
            player.openInventory(getInventory());
        } else {
            Scheduling.entity(player, () -> player.openInventory(getInventory()));
        }
    }

    default void handleClick(InventoryClickEvent event) {}

    default void handleClose(InventoryCloseEvent event) {}
}
