package dev.lumas.shops.components.data;

import dev.lumas.shops.constants.MarketSlot;
import dev.lumas.shops.interfaces.Meta;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Getter
@NullMarked
@RequiredArgsConstructor
@Accessors(fluent = true)
public class SlotEntry {

    private final int slot;
    private final MarketSlot type;
    private final ItemStack stack;

    @Nullable
    private transient ItemStack displayStack;


    public MarketSlot type() {
        if (type == MarketSlot.CONTENT) {
            throw new IllegalStateException("Static slot types cannot be CONTENT.");
        }
        return type;
    }

    public ItemStack displayStack() {
        // Can't use lazy here because of Gson's unsafe allocation weirdness
        if (displayStack == null) {
            displayStack = Meta.edit(stack.clone(), meta -> {
                meta.setHideTooltip(true);
            });
        }

        return displayStack;
    }
}