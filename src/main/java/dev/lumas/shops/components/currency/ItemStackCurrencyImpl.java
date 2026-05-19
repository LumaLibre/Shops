package dev.lumas.shops.components.currency;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// TODO: Implement
public record ItemStackCurrencyImpl(ItemStackAmount amount) implements Currency<Integer> {

    @Override
    public Integer getBalance(Player player) {
        return 0;
    }

    @Override
    public boolean withdraw(Player player, Integer amount) {
        return false;
    }

    @Override
    public Currencies type() {
        return Currencies.ITEMSTACK;
    }

    @Override
    public Object get() {
        return amount;
    }

    public record ItemStackAmount(ItemStack itemStack, int amount) {
    }
}
