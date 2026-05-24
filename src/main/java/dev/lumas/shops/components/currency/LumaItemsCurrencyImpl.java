package dev.lumas.shops.components.currency;

import com.google.common.base.Preconditions;
import dev.lumas.lumaitems.LumaItems;
import dev.lumas.lumaitems.api.LumaItemsAPI;
import dev.lumas.lumaitems.model.item.CustomItem;
import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.util.ClassUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public record LumaItemsCurrencyImpl(LumaItemsAmount amount) implements Currency<Integer> {

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
    public boolean withdraw(Player player) {
        NamespacedKey namespacedKey = new NamespacedKey(LumaItems.getInstance(), amount.key());
        PlayerInventory inventory = player.getInventory();

        int amount = this.amount.amount();
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
    public Currencies type() {
        return Currencies.LUMAITEMS;
    }

    @Override
    public Object get() {
        return amount;
    }

    @Override
    public Component readablePrice() {
        CustomItem customItem = LumaItemsAPI.getInstance().getCustomItem(amount.key());
        // TODO: Fail gracefully
        Preconditions.checkNotNull(customItem, "Custom item with key " + amount.key() + " does not exist");

        ItemStack itemStack = customItem.createItem().getSecond();
        Component customName = itemStack.getItemMeta().customName();
        Component name = customName != null ? customName : Component.text(ClassUtil.formatEnum(itemStack.getType()));
        return Component.text(amount.amount() + "x ").append(name);
    }

    @Override
    public Integer price() {
        return amount.amount();
    }

    public static LumaItemsCurrencyImpl of(String key, int amount) {
        return new LumaItemsCurrencyImpl(new LumaItemsAmount(key, amount));
    }

    public record LumaItemsAmount(String key, int amount) {
    }
}
