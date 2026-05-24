package dev.lumas.shops.interfaces;

import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.constants.suppliers.Products;
import org.bukkit.entity.Player;

/**
 * Represents a product.
 * A product is an item that can be purchased from the market.
 */
public interface Product extends EnumType<Products> {
    void give(Player player, MarketItem marketItem, int amount);
}
