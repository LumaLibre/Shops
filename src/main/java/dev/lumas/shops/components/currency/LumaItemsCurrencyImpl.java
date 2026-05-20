package dev.lumas.shops.components.currency;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

// TODO: Implement
public record LumaItemsCurrencyImpl(LumaItemsAmount amount) implements Currency<Integer> {

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
        return Currencies.LUMAITEMS;
    }

    @Override
    public Object get() {
        return amount;
    }

    @Override
    public String price() {
        return amount.amount() + "x " + amount.key(); // TODO: Implement
    }

    public record LumaItemsAmount(String key, int amount) {
    }
}
