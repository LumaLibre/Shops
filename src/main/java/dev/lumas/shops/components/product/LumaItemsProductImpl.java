package dev.lumas.shops.components.product;

import com.google.common.base.Preconditions;
import dev.lumas.lumaitems.api.LumaItemsAPI;
import dev.lumas.lumaitems.model.item.CustomItem;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.EnumType;
import dev.lumas.shops.interfaces.Product;
import org.bukkit.entity.Player;

public record LumaItemsProductImpl(String key) implements Product, EnumType<Products> {

    @Override
    public Products type() {
        return Products.LUMAITEMS;
    }

    @Override
    public void give(Player player, MarketItem marketItem, int amount) {
        CustomItem customItem = LumaItemsAPI.getInstance().getCustomItem(key);
        Preconditions.checkNotNull(customItem, "Custom item with key " + key + " does not exist");

        // FIXME: Exception when amount > than max stack size
        player.give(customItem.createItem().getSecond().asQuantity(amount));
    }

    @Override
    public Object get() {
        return key;
    }
}
