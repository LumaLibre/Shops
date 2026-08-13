package dev.lumas.shops.components.currency;

import dev.lumas.shops.api.currency.CurrencyType;
import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.util.ClassUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.Map;

public record ItemStackCurrencyImpl(ItemStackAmount amount) implements Currency<Integer> {

    @Override
    public Integer getBalance(Player player) {
        return player.getInventory().all(amount.itemStack().getType()).values().stream()
                .filter(itemStack -> itemStack.isSimilar(amount.itemStack()))
                .mapToInt(ItemStack::getAmount)
                .sum();
    }

    @Override
    public boolean withdraw(Player player, int multiplier) {
        return takeItem(player, amount.itemStack(), amount.amount() * multiplier);
    }

    @Override
    public CurrencyType<?> type() {
        return Currencies.ITEMSTACK;
    }

    @Override
    public Object get() {
        return amount;
    }

    @Override
    public Component readablePrice(int multiplier) {
        ItemStack itemStack = amount.itemStack();
        Component customName = itemStack.getItemMeta().customName();
        Component name = customName != null ? customName : Component.text(ClassUtil.formatEnum(itemStack.getType()));
        return Component.text((amount.amount() * multiplier) + "x ").append(name);
    }

    @Override
    public Integer price() {
        return amount.amount();
    }

    public static boolean takeItem(Player player, ItemStack itemStack, int amount) {
        if (player == null) {
            return false;
        }

        PlayerInventory inventory = player.getInventory();
        if (!inventory.containsAtLeast(itemStack, amount)) {
            return false;
        }

        Map<Integer, ItemStack> couldNotRemove = inventory.removeItemAnySlot(itemStack.asQuantity(amount));
        if (couldNotRemove.isEmpty()) {
            return true;
        }
        throw new RuntimeException("Failed to remove: " + couldNotRemove + " from " + player.getName() + "'s inventory!");
    }

    public static ItemStackCurrencyImpl of(ItemStack itemStack, int amount) {
        return new ItemStackCurrencyImpl(new ItemStackAmount(itemStack.asOne(), amount));
    }

    public record ItemStackAmount(ItemStack itemStack, int amount) {
    }
}
