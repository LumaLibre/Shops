package dev.lumas.shops.components;

import dev.lumas.shops.components.data.SlotEntry;
import dev.lumas.shops.components.dialog.ConfirmationDialog;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.constants.MarketSlot;
import dev.lumas.shops.interfaces.ShopsInventory;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

// Credits: BreweryTeam/BreweryRecipes/RecipesGui.kt

@Getter
@NullMarked
@Accessors(fluent = true)
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Market implements ShopsInventory {

    @Delegate
    private final MarketTemplate template;
    @Delegate
    private final MarketState state;

    private final Locale locale;

    @Accessors(fluent = false)
    private final Inventory inventory;
    private final Map<Integer, MarketSlot> slotTypes = new HashMap<>();
    private final Map<Integer, MarketItem> slotItems = new HashMap<>();

    private int page = 0;

    public Market(MarketTemplate template, MarketState state, Locale locale) {
        this.template = template;
        this.state = state;
        this.locale = locale;
        this.inventory = Bukkit.createInventory(this, template.size(), template.title());
        this.render();
    }

    @Override
    public Key key() {
        return template.key(); // Explicitly return the template key
    }

    public int pageCapacity() {
        return template.contentSlots().size();
    }

    public int pageCount() {
        List<MarketItem> items = template.itemList();
        if (items.isEmpty()) return 1;
        int capacity = pageCapacity();
        if (capacity == 0) return 1;
        return (items.size() + capacity - 1) / capacity;
    }

    public boolean hasNextPage() {
        return page < pageCount() - 1;
    }

    public boolean hasPreviousPage() {
        return page > 0;
    }

    public void nextPage() {
        if (!hasNextPage()) return;
        page++;
        this.render();
    }

    public void previousPage() {
        if (!hasPreviousPage()) return;
        page--;
        this.render();
    }

    public void render() {
        inventory.clear();
        slotTypes.clear();
        slotItems.clear();

        for (SlotEntry entry : template.staticSlots()) {
//            if (entry.type() == MarketSlot.PREVIOUS_PAGE && !hasPreviousPage()) continue;
//            if (entry.type() == MarketSlot.NEXT_PAGE && !hasNextPage()) continue;
            inventory.setItem(entry.slot(), entry.displayStack());
            slotTypes.put(entry.slot(), entry.type());
        }

        List<MarketItem> items = template.itemList();
        List<Integer> contentSlots = template.contentSlots().slots();
        int capacity = contentSlots.size();
        if (capacity == 0) return;

        int start = page * capacity;
        int end = Math.min(start + capacity, items.size());
        for (int i = start; i < end; i++) {
            int slot = contentSlots.get(i - start);
            MarketItem item = items.get(i);
            inventory.setItem(slot, item.display(state, locale));
            slotTypes.put(slot, MarketSlot.CONTENT);
            slotItems.put(slot, item);
        }
    }

    public void refreshSlot(int slot) {
        MarketItem item = slotItems.get(slot);
        if (item == null) return;
        inventory.setItem(slot, item.display(state, locale));
    }

    public void refreshItem(MarketItem item) {
        slotItems.forEach((slot, slotItem) -> {
            if (slotItem == item) {
                inventory.setItem(slot, item.display(state, locale));
            }
        });
    }

    public MarketSlot typeAt(int slot) {
        return slotTypes.getOrDefault(slot, MarketSlot.BORDER);
    }

    @Nullable
    public MarketItem itemAt(int slot) {
        return slotItems.get(slot);
    }

    /**
     * Jumps to the given page index. Out-of-range values are clamped to
     * {@code [0, pageCount() - 1]}. Re-renders only if the page actually changed.
     *
     * @return {@code true} if the page changed
     */
    public boolean setPage(int page) {
        int clamped = Math.max(0, Math.min(page, pageCount() - 1));
        if (clamped == this.page) return false;
        this.page = clamped;
        this.render();
        return true;
    }

    /**
     * Jumps to the page containing the given item. No-op if the item is not
     * present in this market.
     *
     * @return {@code true} if the page changed
     */
    public boolean setPage(MarketItem item) {
        List<MarketItem> items = template.itemList();
        int index = items.indexOf(item);
        if (index < 0) return false;
        int capacity = pageCapacity();
        if (capacity == 0) return false;
        return setPage(index / capacity);
    }

    public void prePurchase(MarketItem marketItem, Player player, boolean showAmountSelector) {
        ConfirmationDialog dialog = new ConfirmationDialog(player, this, marketItem, showAmountSelector);
        dialog.show(player);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        MarketSlot type = typeAt(slot);
        Player player = (Player) event.getWhoClicked();

        switch (type) {
            case CONTENT -> {
                MarketItem item = itemAt(slot);
                if (item != null) {
                    this.prePurchase(item, player, event.getClick().isRightClick());
                }
            }
            case PREVIOUS_PAGE -> previousPage();
            case NEXT_PAGE -> nextPage();
            case CLOSE -> player.closeInventory();
            case BORDER -> {}
        }
    }
}