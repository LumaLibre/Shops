package dev.lumas.shops.listeners;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.interfaces.ShopsInventory;
import dev.lumas.shops.util.Scheduling;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.inventory.InventoryHolder;

@Register(Autowire.LISTENER)
public class PluginDisableListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginDisable(PluginDisableEvent event) {
        if (!event.getPlugin().equals(Shops.instance())) {
            return;
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            Scheduling.entity(player, () -> {
                InventoryHolder openInvHolder = player.getOpenInventory().getTopInventory().getHolder();
                if (openInvHolder instanceof ShopsInventory) {
                    player.closeInventory();
                    Shops.instance().getLogger().warning("Closed inventory for player " + player.getName() + " was this a reload?");
                }
            });
        }
    }
}
