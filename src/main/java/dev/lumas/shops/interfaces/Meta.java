package dev.lumas.shops.interfaces;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Functional interface for editing item meta.
 */
@FunctionalInterface
public interface Meta {

    /**
     * Edits the item meta.
     * @param meta The item meta to edit.
     */
    void edit(ItemMeta meta);

    /**
     * Edits the item meta of an item stack.
     * @param itemStack The item stack to edit.
     * @param meta The item meta to edit.
     * @return The edited item stack.
     */
    static ItemStack edit(ItemStack itemStack, Meta meta) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta != null) {
            meta.edit(itemMeta);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }
}
