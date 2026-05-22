package dev.lumas.shops.interfaces;

import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.constants.suppliers.Products;
import org.bukkit.entity.Player;

public interface Product extends EnumType<Products> {
    void give(Player player, MarketItem marketItem, int amount);
}
