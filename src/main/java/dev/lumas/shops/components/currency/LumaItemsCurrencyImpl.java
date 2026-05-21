package dev.lumas.shops.components.currency;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// TODO: Implement
public record LumaItemsCurrencyImpl(LumaItemsAmount amount) implements Currency<Integer> {

    @Override
    public Integer getBalance(Player player) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean withdraw(Player player) {
        throw new UnsupportedOperationException("Not implemented yet.");
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
    public String readablePrice() {
        return amount.amount() + "x " + amount.key(); // TODO: Implement
    }

    @Override
    public Integer price() {
        return amount.amount();
    }

    public record LumaItemsAmount(String key, int amount) {
    }
}
