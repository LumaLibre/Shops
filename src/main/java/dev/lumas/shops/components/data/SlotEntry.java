package dev.lumas.shops.components.data;

import dev.lumas.shops.constants.MarketSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SlotEntry(int slot, MarketSlot type, ItemStack stack) {

    @Override
    public MarketSlot type() {
        if (type == MarketSlot.CONTENT) {
            throw new IllegalStateException("Static slot types cannot be CONTENT.");
        }
        return type;
    }
}