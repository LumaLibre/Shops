package dev.lumas.shops.interfaces;

import dev.lumas.shops.Shops;
import dev.lumas.shops.util.Scheduling;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ShopsInventory extends InventoryHolder {

    default void open(Player player) {
        if (Bukkit.isOwnedByCurrentRegion(player)) {
            player.openInventory(getInventory());
        } else {
            Scheduling.entity(player, () -> player.openInventory(getInventory()));
        }
    }
}
