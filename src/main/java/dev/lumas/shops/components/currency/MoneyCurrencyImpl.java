package dev.lumas.shops.components.currency;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import org.bukkit.entity.Player;

// TODO: Implement
public record MoneyCurrencyImpl(double cost) implements Currency<Double> {

    @Override
    public Double getBalance(Player player) {
        return 0.0;
    }

    @Override
    public boolean withdraw(Player player, Double amount) {
        return false;
    }

    @Override
    public Currencies type() {
        return Currencies.MONEY;
    }

    @Override
    public Object get() {
        return cost;
    }
}
