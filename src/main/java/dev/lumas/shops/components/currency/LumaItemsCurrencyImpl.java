package dev.lumas.shops.components.currency;

import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.lumaitems.LumaItems;
import dev.lumas.lumaitems.api.LumaItemsAPI;
import dev.lumas.lumaitems.model.item.CustomItem;
import dev.lumas.shops.api.currency.CurrencyType;
import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.util.ClassUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jspecify.annotations.Nullable;

public record LumaItemsCurrencyImpl(LumaItemsAmount amount) implements Currency<Integer> {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    @Override
    public Integer getBalance(Player player) {
        PlayerInventory inventory = player.getInventory();
        NamespacedKey namespacedKey = new NamespacedKey(LumaItems.getInstance(), amount.key());
        int total = 0;

        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || !itemStack.hasItemMeta()) {
                continue;
            }

            if (itemStack.getPersistentDataContainer().has(namespacedKey)) {
                total += itemStack.getAmount();
            }
        }
        return total;
    }

    @Override
    public boolean withdraw(Player player, int multiplier) {
        NamespacedKey namespacedKey = new NamespacedKey(LumaItems.getInstance(), amount.key());
        PlayerInventory inventory = player.getInventory();

        int amount = this.amount.amount() * multiplier;
        int total = getBalance(player);


        if (total < amount) {
            return false;
        }

        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || !itemStack.hasItemMeta()) {
                continue;
            }

            if (itemStack.getPersistentDataContainer().has(namespacedKey)) {
                int toRemove = Math.min(itemStack.getAmount(), amount);
                itemStack.setAmount(itemStack.getAmount() - toRemove);
                amount -= toRemove;

                if (amount <= 0) {
                    return true;
                }
            }
        }
        throw new RuntimeException("Failed to remove: " + amount + "/" + namespacedKey + " from " + player.getName() + "'s inventory!");
    }

    @Override
    public CurrencyType<?> type() {
        return Currencies.LUMAITEMS;
    }

    @Override
    public Object get() {
        return amount;
    }

    @Override
    public boolean isAvailable() {
        return customItem() != null;
    }

    @Override
    public Component readablePrice(int multiplier) {
        int total = amount.amount() * multiplier;
        CustomItem customItem = customItem();
        if (customItem == null) {
            LOGGER.warning("Cannot price in LumaItem '" + amount.key() + "': no custom item with that key exists");
            return Component.text(total + "x " + amount.key());
        }

        ItemStack itemStack = customItem.createItem().getSecond();
        Component customName = itemStack.getItemMeta().customName();
        Component name = customName != null ? customName : Component.text(ClassUtil.formatEnum(itemStack.getType()));
        return Component.text(total + "x ").append(name);
    }

    @Override
    public Integer price() {
        return amount.amount();
    }

    private @Nullable CustomItem customItem() {
        return LumaItemsAPI.getInstance().getCustomItem(amount.key());
    }

    public static LumaItemsCurrencyImpl of(String key, int amount) {
        return new LumaItemsCurrencyImpl(new LumaItemsAmount(key, amount));
    }

    public record LumaItemsAmount(String key, int amount) {
    }
}
