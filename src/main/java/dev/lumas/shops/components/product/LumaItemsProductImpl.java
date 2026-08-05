package dev.lumas.shops.components.product;

import com.google.common.base.Preconditions;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.lumaitems.api.LumaItemsAPI;
import dev.lumas.lumaitems.model.item.CustomItem;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.config.ShopsConfig;
import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.EnumType;
import dev.lumas.shops.interfaces.Product;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public record LumaItemsProductImpl(String key) implements Product, EnumType<Products> {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    @Override
    public Products type() {
        return Products.LUMAITEMS;
    }

    @Override
    public void give(Player player, MarketItem marketItem, int amount) {
        CustomItem customItem = LumaItemsAPI.getInstance().getCustomItem(key);
        Preconditions.checkNotNull(customItem, "Custom item with key " + key + " does not exist");

        ItemStack template = customItem.createItem().getSecond();
        int maxStackSize = template.getMaxStackSize();
        int remaining = amount;

        while (remaining > 0) {
            int stackAmount = Math.min(remaining, maxStackSize);
            player.give(template.asQuantity(stackAmount));
            remaining -= stackAmount;
        }
    }

    @Override
    public ItemStack render(ItemStack stored) {
        // Checked first so nothing touches the LumaItems API while the feature is off.
        if (!ShopsConfig.instance().lumaItemsRender()) {
            return stored;
        }

        CustomItem customItem = LumaItemsAPI.getInstance().getCustomItem(key);
        if (customItem == null) {
            LOGGER.warning("Cannot render LumaItem '" + key + "': no custom item with that key exists");
            return stored;
        }
        return customItem.createItem().getSecond().asQuantity(stored.getAmount());
    }

    @Override
    public Object get() {
        return key;
    }
}
