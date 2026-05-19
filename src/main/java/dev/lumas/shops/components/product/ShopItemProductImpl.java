package dev.lumas.shops.components.product;

import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.Product;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record ShopItemProductImpl(ItemStack itemStack) implements Product {

    @Override
    public void give(Player player, int amount) {
        player.give(itemStack.asQuantity(amount));
    }

    @Override
    public Products type() {
        return Products.SHOP_ITEM;
    }

    @Override
    public Object get() {
        return itemStack;
    }
}
