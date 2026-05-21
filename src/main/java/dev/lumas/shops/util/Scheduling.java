package dev.lumas.shops.util;

import dev.lumas.shops.Shops;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

public final class Scheduling {

    private static final Shops INSTANCE = Shops.instance();

    public static ScheduledTask global(Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().run(INSTANCE, t -> runnable.run());
    }

    public static ScheduledTask entity(Entity entity, Runnable runnable) {
        return entity.getScheduler().run(INSTANCE, t -> runnable.run(), null);
    }
}
