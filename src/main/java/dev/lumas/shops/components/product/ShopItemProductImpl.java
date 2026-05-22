package dev.lumas.shops.components.product;

import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.Product;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ShopItemProductImpl() implements Product {

    @Override
    public void give(Player player, MarketItem marketItem, int amount) {
        player.give(marketItem.stack().asQuantity(amount));
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
