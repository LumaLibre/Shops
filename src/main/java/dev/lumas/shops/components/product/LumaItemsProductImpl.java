package dev.lumas.shops.components.product;

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
import org.jspecify.annotations.Nullable;

public record LumaItemsProductImpl(String key) implements Product, EnumType<Products> {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    @Override
    public Products type() {
        return Products.LUMAITEMS;
    }

    @Override
    public boolean isAvailable() {
        return customItem() != null;
    }

    @Override
    public void give(Player player, MarketItem marketItem, int amount) {
        CustomItem customItem = customItem();
        if (customItem == null) {
            // Purchases of an unavailable item are refused before anything is charged, so this
            // only trips if the key went away mid-purchase. Nothing to hand out either way.
            LOGGER.error("Cannot give LumaItem '" + key + "' to " + player.getName()
                    + ": no custom item with that key exists");
            return;
        }

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

        CustomItem customItem = customItem();
        if (customItem == null) {
            LOGGER.warning("Cannot render LumaItem '" + key + "': no custom item with that key exists");
            return stored;
        }
        return customItem.createItem().getSecond().asQuantity(stored.getAmount());
    }

    private @Nullable CustomItem customItem() {
        return LumaItemsAPI.getInstance().getCustomItem(key);
    }

    @Override
    public Object get() {
        return key;
    }
}
