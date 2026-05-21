package dev.lumas.shops.components.templates;

import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.SlotEntry;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * Immutable definition of a market loaded from {@code markets/<key>.json}.
 * Everything an admin defines externally lives here.
 *
 * <p>Pair with a {@link MarketState} (mutable, shared receipts) to construct a
 * per-player {@link Market} via {@link dev.lumas.shops.MarketManager#open}.
 */
@Getter
@NullMarked
@Accessors(fluent = true)
public class MarketTemplate implements Keyed {

    private final Key key;
    private final Component title;
    private final int size;
    private final List<SlotEntry> staticSlots;
    private final List<MarketItem> items;

    public MarketTemplate(Key key, Component title, int size, List<SlotEntry> staticSlots, List<MarketItem> items) {
        this.key = key;
        this.title = title;
        this.size = size;
        this.staticSlots = List.copyOf(staticSlots);
        this.items = List.copyOf(items);
    }

    public Market toMarket(MarketState state) {
        return new Market(this, state);
    }
}