package dev.lumas.shops.components;

import dev.lumas.shops.components.serial.SerialComponent;
import dev.lumas.shops.interfaces.Transformable;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

// Credits: BreweryTeam/BreweryRecipes/RecipesGui.kt

@Getter
@NullMarked
@Accessors(fluent = true)
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Market implements Transformable<Inventory>, InventoryHolder {

    private final int size;
    private final SerialComponent title;

    private final List<SlotEntry> staticSlots; // borders + buttons, passed in
    private final List<MarketItem> items;

    private final transient Inventory inv;
    private final transient List<Integer> contentSlots; // computed: everything not claimed by staticSlots
    private final transient Map<Integer, MarketSlot> slotTypes = new HashMap<>();
    private final transient Map<Integer, MarketItem> slotItems = new HashMap<>();

    private int page = 0;

    public Market(int size, SerialComponent title, List<SlotEntry> staticSlots, List<MarketItem> items) {
        this.size = size;
        this.title = title;
        this.staticSlots = staticSlots;
        this.items = items;
        this.inv = Bukkit.createInventory(this, size, title.get());
        this.contentSlots = this.computeContentSlots();
        this.render();
    }

    private List<Integer> computeContentSlots() {
        var claimed = staticSlots.stream().map(SlotEntry::slot).collect(Collectors.toSet());
        return IntStream.range(0, size)
                .filter(i -> !claimed.contains(i))
                .boxed()
                .toList();
    }

    public int pageCapacity() {
        return contentSlots.size();
    }

    public int pageCount() {
        if (items.isEmpty()) return 1;
        return (items.size() + pageCapacity() - 1) / pageCapacity();
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
        inv.clear();
        slotTypes.clear();
        slotItems.clear();

        for (SlotEntry entry : staticSlots) {
            if (entry.type() == MarketSlot.PREVIOUS_PAGE && !hasPreviousPage()) continue;
            if (entry.type() == MarketSlot.NEXT_PAGE && !hasNextPage()) continue;
            inv.setItem(entry.slot(), entry.stack().transform());
            slotTypes.put(entry.slot(), entry.type());
        }

        int start = page * pageCapacity();
        int end = Math.min(start + pageCapacity(), items.size());
        for (int i = start; i < end; i++) {
            int slot = contentSlots.get(i - start);
            MarketItem item = items.get(i);
            item.stack().
            inv.setItem(slot, item.stack().transform());
            slotTypes.put(slot, MarketSlot.CONTENT);
            slotItems.put(slot, item);
        }
    }

    public MarketSlot typeAt(int slot) {
        return slotTypes.getOrDefault(slot, MarketSlot.BORDER);
    }

    public MarketItem itemAt(int slot) {
        return slotItems.get(slot);
    }

    @Override
    public Inventory transform() {
        return inv;
    }

    @Override
    public Inventory getInventory() {
        return inv;
    }
}