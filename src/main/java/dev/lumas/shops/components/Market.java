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
import net.kyori.adventure.key.Keyed;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

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
public class Market implements ShopsInventory, Keyed {

    @Delegate
    private final MarketTemplate template;
    @Delegate
    private final MarketState state;

    @Accessors(fluent = false)
    private final Inventory inventory;
    private final List<Integer> contentSlots; // computed: everything not claimed by static slots
    private final Map<Integer, MarketSlot> slotTypes = new HashMap<>();
    private final Map<Integer, MarketItem> slotItems = new HashMap<>();

    private int page = 0;

    public Market(MarketTemplate template, MarketState state) {
        this.template = template;
        this.state = state;
        this.inventory = Bukkit.createInventory(this, template.size(), template.title());
        this.contentSlots = this.computeContentSlots();
        this.render();
    }

    private List<Integer> computeContentSlots() {
        var claimed = template.staticSlots().stream().map(SlotEntry::slot).collect(Collectors.toSet());
        return IntStream.range(0, template.size())
                .filter(i -> !claimed.contains(i))
                .boxed()
                .toList();
    }


    public int pageCapacity() {
        return contentSlots.size();
    }

    public int pageCount() {
        List<MarketItem> items = template.items();
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
        inventory.clear();
        slotTypes.clear();
        slotItems.clear();

        for (SlotEntry entry : template.staticSlots()) {
            if (entry.type() == MarketSlot.PREVIOUS_PAGE && !hasPreviousPage()) continue;
            if (entry.type() == MarketSlot.NEXT_PAGE && !hasNextPage()) continue;
            inventory.setItem(entry.slot(), entry.stack());
            slotTypes.put(entry.slot(), entry.type());
        }

        List<MarketItem> items = template.items();
        int start = page * pageCapacity();
        int end = Math.min(start + pageCapacity(), items.size());
        for (int i = start; i < end; i++) {
            int slot = contentSlots.get(i - start);
            MarketItem item = items.get(i);
            inventory.setItem(slot, item.stack());
            slotTypes.put(slot, MarketSlot.CONTENT);
            slotItems.put(slot, item);
        }
    }

    public MarketSlot typeAt(int slot) {
        return slotTypes.getOrDefault(slot, MarketSlot.BORDER);
    }

    @Nullable
    public MarketItem itemAt(int slot) {
        return slotItems.get(slot);
    }


    public void prePurchase(MarketItem marketItem, Player player) {
        ConfirmationDialog dialog = new ConfirmationDialog(this, marketItem);
        dialog.show(player);
    }

    public void handleClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        MarketSlot type = typeAt(slot);
        Player player = (Player) event.getWhoClicked();
        event.setCancelled(true);

        switch (type) {
            case CONTENT -> {
                MarketItem item = itemAt(slot);
                if (item != null) {
                    this.prePurchase(item, player);
                }
            }
            case PREVIOUS_PAGE -> previousPage();
            case NEXT_PAGE -> nextPage();
            case CLOSE -> player.closeInventory();
            case BORDER -> {}
        }
    }
}