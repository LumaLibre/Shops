package dev.lumas.shops.components;

import dev.lumas.shops.components.serial.SerialStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record SlotEntry(int slot, SerialStack stack, MarketSlot type) {}