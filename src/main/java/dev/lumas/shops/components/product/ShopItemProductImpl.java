package dev.lumas.shops.components.product;

import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.Product;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ShopItemProductImpl() implements Product {

    @Override
    public void give(Player player, MarketItem marketItem, int amount) {
        ItemStack template = marketItem.stack();
        int maxStackSize = template.getMaxStackSize();
        int remaining = amount;

        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxStackSize);
            player.give(template.asQuantity(stackAmount));
            remaining -= stackAmount;
        }
    }

    @Override
    public Products type() {
        return Products.SHOP_ITEM;
    }

    @Override
    public @Nullable Object get() {
        return null;
    }
}
