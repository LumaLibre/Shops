package dev.lumas.shops.interfaces;

import dev.lumas.shops.components.data.SlotEntry;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.constants.MarketSlot;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Credits: BreweryTeam/BreweryRecipes/RecipesGui.kt

@Getter
@NullMarked
@Accessors(fluent = true)
public abstract class PaginatedInventory<T> implements ShopsInventory {

    protected final MarketTemplate template;

    @Accessors(fluent = false)
    protected final Inventory inventory;
    protected final Map<Integer, MarketSlot> slotTypes = new HashMap<>();
    protected final Map<Integer, T> slotItems = new HashMap<>();

    protected int page = 0;

    protected PaginatedInventory(MarketTemplate template) {
        this.template = template;
        this.inventory = Bukkit.createInventory(this, size(), title());
    }

    protected int size() {
        return template.size();
    }

    protected Component title() {
        return template.title();
    }

    /**
     * The full list of items to paginate across pages.
     */
    protected abstract List<T> items();

    /**
     * Renders a single item into an ItemStack for display in the inventory.
     */
    protected abstract ItemStack renderItem(T item);

    /**
     * Called when a content slot is clicked. Event is cancelled by default.
     */
    protected abstract void onContentClick(T item, InventoryClickEvent event);

    public int pageCapacity() {
        return template.contentSlots().size();
    }

    public int pageCount() {
        List<T> items = items();
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
            inventory.setItem(entry.slot(), entry.displayStack());
            slotTypes.put(entry.slot(), entry.type());
        }

        List<T> items = items();
        List<Integer> contentSlots = template.contentSlots().slots();
        int capacity = contentSlots.size();
        if (capacity == 0) return;

        int start = page * capacity;
        int end = Math.min(start + capacity, items.size());
        for (int i = start; i < end; i++) {
            int slot = contentSlots.get(i - start);
            T item = items.get(i);
            inventory.setItem(slot, renderItem(item));
            slotTypes.put(slot, MarketSlot.CONTENT);
            slotItems.put(slot, item);
        }
    }

    public void refreshSlot(int slot) {
        T item = slotItems.get(slot);
        if (item == null) return;
        inventory.setItem(slot, renderItem(item));
    }

    public void refreshItem(T item) {
        slotItems.forEach((slot, slotItem) -> {
            if (slotItem == item) {
                inventory.setItem(slot, renderItem(item));
            }
        });
    }

    public MarketSlot typeAt(int slot) {
        return slotTypes.getOrDefault(slot, MarketSlot.BORDER);
    }

    @Nullable
    public T itemAt(int slot) {
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
     * present.
     *
     * @return {@code true} if the page changed
     */
    public boolean setPage(T item) {
        List<T> items = items();
        int index = items.indexOf(item);
        if (index < 0) return false;
        int capacity = pageCapacity();
        if (capacity == 0) return false;
        return setPage(index / capacity);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        int slot = event.getRawSlot();
        MarketSlot type = typeAt(slot);
        Player player = (Player) event.getWhoClicked();

        switch (type) {
            case CONTENT -> {
                T item = itemAt(slot);
                if (item != null) {
                    onContentClick(item, event);
                }
            }
            case PREVIOUS_PAGE -> previousPage();
            case NEXT_PAGE -> nextPage();
            case CLOSE -> player.closeInventory();
            case BORDER -> {}
        }
    }
}