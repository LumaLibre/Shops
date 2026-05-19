package dev.lumas.shops.components.data;

import dev.lumas.shops.constants.MarketSlot;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SlotEntry(int slot, ItemStack stack, MarketSlot type) {}