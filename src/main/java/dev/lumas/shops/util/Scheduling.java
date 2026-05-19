package dev.lumas.shops.util;

import dev.lumas.shops.Shops;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;

public final class Scheduling {

    private static final Shops INSTANCE = Shops.instance();

    public static ScheduledTask global(Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().run(INSTANCE, t -> runnable.run());
    }
}
