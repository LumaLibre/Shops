package dev.lumas.shops.util;

import dev.lumas.shops.Shops;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;

import java.util.function.Consumer;

@UtilityClass
public final class Scheduling {

    private static final Shops INSTANCE = Shops.instance();

    public static ScheduledTask global(Runnable runnable) {
        return Bukkit.getGlobalRegionScheduler().run(INSTANCE, t -> runnable.run());
    }

    public static ScheduledTask entity(Entity entity, Runnable runnable) {
        return entity.getScheduler().run(INSTANCE, t -> runnable.run(), null);
    }

    public static ScheduledTask entityTimer(Entity entity, long delay, long period, Consumer<ScheduledTask> consumer) {
        return entity.getScheduler().runAtFixedRate(INSTANCE, consumer, null, delay, period);
    }

    public static ScheduledTask entityDelayed(Entity entity, long delay, Runnable runnable) {
        return entity.getScheduler().runDelayed(INSTANCE, t -> runnable.run(), null, delay);
    }
}
