package dev.lumas.shops.listeners;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.Shops;
import dev.lumas.shops.interfaces.ShopsInventory;
import dev.lumas.shops.util.InventoryUtil;
import dev.lumas.shops.util.Scheduling;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Register(Autowire.LISTENER)
public class PluginDisableListener implements Listener {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    // Using this event instead of #onDisable() to close all open inventories on Folia
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPluginDisable(PluginDisableEvent event) {
        if (!event.getPlugin().equals(Shops.instance())) {
            return;
        }

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        InventoryUtil.iterateOpen((player, inventory) -> {
            if (inventory.getHolder(false) instanceof ShopsInventory) {
                CompletableFuture<Void> future = new CompletableFuture<>();
                Scheduling.entity(player, () -> {
                    try {
                        player.closeInventory();
                    } finally {
                        future.complete(null);
                    }
                });
                futures.add(future);
            }
        });

        // Forcefully block until we're done closing all inventories
        LOGGER.info("Closing all open inventories...");
        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
        LOGGER.info("All open inventories closed.");
    }
}