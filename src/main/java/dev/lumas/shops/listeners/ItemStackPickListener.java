package dev.lumas.shops.listeners;

import com.google.common.base.Preconditions;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Provided;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.components.dialog.session.ItemStackPickConfirmDialog;
import dev.lumas.shops.components.dialog.session.AddItemSession;
import dev.lumas.shops.util.Scheduling;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Provided
@NullMarked
@Register(Autowire.LISTENER)
public class ItemStackPickListener implements Listener {

    public static final ItemStackPickListener INSTANCE = new ItemStackPickListener();

    private static final int TIMEOUT_TICKS = 300;          // 15 seconds
    private static final int TITLE_INTERVAL_TICKS = 20;        // 1 second
    private static final Title.Times TITLE_TIMES = Title.Times.times(
            Duration.ZERO,
            Duration.ofSeconds(2),  // longer than the interval so titles don't flicker
            Duration.ZERO
    );

    private final Map<UUID, Pending> pendingMap = new ConcurrentHashMap<>();


    public void begin(AddItemSession session, Player player, Runnable onComplete, Runnable onCancel) {
        Preconditions.checkState(!pendingMap.containsKey(player.getUniqueId()), "Player %s already has a pending item stack pick", player.getName());

        Title title = Title.title(
                Component.translatable("shops.additem.itemstack.title"),
                Component.translatable("shops.additem.itemstack.subtitle"),
                TITLE_TIMES
        );

        AtomicInteger ticksElapsed = new AtomicInteger();

        Scheduling.entityTimer(player, 1L, TITLE_INTERVAL_TICKS, task -> {
            // If the player removed themselves (clicked) or went offline, just stop the task.
            // Don't fire onCancel — the click handler owns the followup.
            if (!pendingMap.containsKey(player.getUniqueId()) || !player.isOnline()) {
                task.cancel();
                player.clearTitle();
                return;
            }

            if (ticksElapsed.get() >= TIMEOUT_TICKS) {
                task.cancel();
                pendingMap.remove(player.getUniqueId());
                player.clearTitle();
                player.sendMessage(Component.translatable("shops.additem.itemstack.timeout"));
                onCancel.run();
                return;
            }

            ticksElapsed.addAndGet(TITLE_INTERVAL_TICKS);
            player.showTitle(title);
        });

        pendingMap.put(player.getUniqueId(), new Pending(session, onComplete, onCancel));
    }

    /** Cancels and removes the session for {@code player}, if any. */
    public void cancel(Player player) {
        Pending p = pendingMap.remove(player.getUniqueId());
        if (p != null) {
            player.clearTitle();
        }
    }


    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Pending pending = pendingMap.get(player.getUniqueId());
        ItemStack clicked = event.getCurrentItem();
        if (pending == null || clicked == null || clicked.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        pendingMap.remove(player.getUniqueId());
        player.clearTitle();
        player.closeInventory();

        // Hand off to a confirm dialog. If the player confirms, set the currency
        // and continue. If they cancel, restart the pick.
        ItemStackPickConfirmDialog dialog = new ItemStackPickConfirmDialog(player.locale(), pending.session(), clicked.clone(), pending.onComplete(), pending.onCancel());
        dialog.show(player);
    }

    private record Pending(AddItemSession session, Runnable onComplete, Runnable onCancel) {}
}