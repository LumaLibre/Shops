package dev.lumas.shops.interfaces;

import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.constants.suppliers.Products;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Represents a product.
 * A product is an item that can be purchased from the market.
 */
public interface Product extends EnumType<Products> {
    void give(Player player, MarketItem marketItem, int amount);

    /**
     * Whether this product can still be handed out. A product pointing at something the server
     * can no longer resolve — a custom item that was deleted, a plugin that was pulled — reports
     * {@code false}, which makes the market item unpurchasable rather than charging a player for
     * something they can't be given.
     * @return True if the product can be given.
     */
    default boolean isAvailable() {
        return true;
    }

    default ItemStack render(ItemStack stored) {
        return stored;
    }
}
