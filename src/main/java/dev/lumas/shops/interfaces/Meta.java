package dev.lumas.shops.interfaces;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@FunctionalInterface
public interface Meta {
    void edit(ItemMeta meta);

    static ItemStack edit(ItemStack itemStack, Meta meta) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            meta.edit(itemMeta);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
