package dev.lumas.shops.constants.suppliers;

import dev.lumas.shops.components.data.TypedEntry;
import dev.lumas.shops.components.product.CommandProductImpl;
import dev.lumas.shops.components.product.LumaItemsProductImpl;
import dev.lumas.shops.components.product.ShopItemProductImpl;
import dev.lumas.shops.interfaces.Product;
import org.bukkit.inventory.ItemStack;

public enum Products {
    COMMAND(new TypedEntry<>(String.class, CommandProductImpl::new)),
    LUMAITEMS(new TypedEntry<>(String.class, LumaItemsProductImpl::new)),
    SHOP_ITEM(new TypedEntry<>(ItemStack.class, ShopItemProductImpl::new));

    private final TypedEntry<?, ? extends Product> entry;

    Products(TypedEntry<?, ? extends Product> entry) {
        this.entry = entry;
    }

    public Product create(Object value) {
        return entry.create(value);
    }

    public Class<?> type() {
        return entry.type();
    }
}
