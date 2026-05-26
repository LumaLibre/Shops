package dev.lumas.shops.components;

import dev.lumas.shops.components.data.PurchaseReceipt;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.Meta;
import dev.lumas.shops.interfaces.PaginatedInventory;
import dev.lumas.shops.util.ItemStacks;
import dev.lumas.shops.util.Scheduling;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static dev.lumas.shops.util.ItemStacks.addLines;

@Getter
@NullMarked
@Accessors(fluent = true)
public class MarketHistory extends PaginatedInventory<MarketHistory.ReceiptDisplay> {

    private final MarketState state;
    private final Locale locale;

    public MarketHistory(MarketTemplate template, MarketState state, Locale locale) {
        super(template);
        this.state = state;
        this.locale = locale;
        this.render();
    }

    @Override
    protected Component title() {
        return Component.translatable("shops.gui.history.title", template.title());
    }

    @Override
    public Key key() {
        return state.key();
    }

    @Override
    protected List<ReceiptDisplay> items() {
        Map<UUID, Map<PurchaseReceipt, Integer>> grouped = new LinkedHashMap<>();

        for (Map.Entry<PurchaseReceipt, Integer> entry : state.receipts().entrySet()) {
            PurchaseReceipt receipt = entry.getKey();
            grouped.computeIfAbsent(receipt.purchaser(), k -> new LinkedHashMap<>())
                    .put(receipt, entry.getValue());
        }

        List<ReceiptDisplay> receiptDisplays = new ArrayList<>(grouped.size());
        for (Map.Entry<UUID, Map<PurchaseReceipt, Integer>> entry : grouped.entrySet()) {
            receiptDisplays.add(new ReceiptDisplay(entry.getKey(), entry.getValue(), template, locale));
        }
        return receiptDisplays;
    }

    @Override
    protected ItemStack renderItem(ReceiptDisplay item) {
        return item.display();
    }

    @Override
    protected void onContentClick(ReceiptDisplay item, InventoryClickEvent event) {

    }

    @Getter
    public static class ReceiptDisplay {

        private final UUID purchaser;
        private final Map<PurchaseReceipt, Integer> receipts;
        private final MarketTemplate template;
        private final Locale locale;

        @MonotonicNonNull
        private ItemStack cached;

        public ReceiptDisplay(UUID purchaser, Map<PurchaseReceipt, Integer> receipts, MarketTemplate template, Locale locale) {
            this.purchaser = purchaser;
            this.receipts = receipts;
            this.template = template;
            this.locale = locale;
        }

        private void buildDisplay() {
            this.cached = Meta.edit(ItemStack.of(Material.PLAYER_HEAD), meta -> {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(purchaser);
                SkullMeta skullMeta = (SkullMeta) meta;
                if (offlinePlayer.isOnline()) {
                    skullMeta.setPlayerProfile(offlinePlayer.getPlayerProfile());
                } else {
                    offlinePlayer.getPlayerProfile().update().thenAccept(profile -> {
                        Scheduling.global(() -> skullMeta.setPlayerProfile(profile));
                    });
                }

                skullMeta.customName(ItemStacks.translate(locale, "shops.gui.history.itemstack.display", offlinePlayer.getName()));

                List<Component> components = new ArrayList<>();

                addLines(components, locale, "shops.gui.history.itemstack.description");
                components.add(Component.empty());

                for (Map.Entry<PurchaseReceipt, Integer> entry : receipts.entrySet()) {
                    PurchaseReceipt receipt = entry.getKey();
                    int amount = entry.getValue();
                    MarketItem item = template.item(receipt.marketItemKey());
                    if (item != null) {
                        addLines(components, locale, "shops.gui.history.itemstack.purchase", item.displayName(), amount);
                    }
                }

                skullMeta.lore(components);
            });
        }

        public ItemStack display() {
            if (cached == null) {
                buildDisplay();
            }
            return cached;
        }
    }
}
