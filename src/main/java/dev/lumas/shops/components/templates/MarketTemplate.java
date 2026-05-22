package dev.lumas.shops.components.templates;

import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.SlotEntry;
import dev.lumas.shops.components.data.SlotList;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Immutable definition of a market loaded from {@code markets/<key>.json}.
 * Everything an admin defines externally lives here.
 */
@Getter
@NullMarked
@Accessors(fluent = true)
public class MarketTemplate implements Keyed {

    private final Key key;
    private final Component title;
    private final int size;
    private final SlotList contentSlots;
    private final List<SlotEntry> staticSlots;
    private final Map<Key, MarketItem> items;

    public MarketTemplate(Key key, Component title, int size, @Nullable SlotList contentSlots, List<SlotEntry> staticSlots, Map<Key, MarketItem> items) {
        this.key = key;
        this.title = title;
        this.size = size;
        this.contentSlots = (contentSlots == null || contentSlots.slots().isEmpty())
                ? defaultContentSlots(size, staticSlots)
                : contentSlots;
        this.staticSlots = List.copyOf(staticSlots);
        this.items = new LinkedHashMap<>(items);
    }

    public Market toMarket(MarketState state) {
        return new Market(this, state);
    }

    public MarketItem item(Key key) {
        return items.get(key);
    }

    public List<MarketItem> itemList() {
        return List.copyOf(items.values());
    }

    private static SlotList defaultContentSlots(int size, List<SlotEntry> staticSlots) {
        Set<Integer> claimed = staticSlots.stream().map(SlotEntry::slot).collect(Collectors.toSet());
        return SlotList.of(IntStream.range(0, size).filter(i -> !claimed.contains(i)).boxed().toList());
    }
}