package dev.lumas.shops.components.currency;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.util.Lazy;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public record MoneyCurrencyImpl(double cost) implements Currency<Double> {

    private static final Lazy<Economy> ECONOMY = Lazy.of(() -> {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            throw new IllegalStateException("No Economy provider registered (is Vault installed?)");
        }
        return rsp.getProvider();
    });

    @Override
    public Double getBalance(Player player) {
        return ECONOMY.get().getBalance(player);
    }

    @Override
    public boolean withdraw(Player player) {
        EconomyResponse response = ECONOMY.get().withdrawPlayer(player, cost);
        return response.transactionSuccess();
    }

    @Override
    public Currencies type() {
        return Currencies.MONEY;
    }

    @Override
    public String readablePrice() {
        return "$" + String.format("%.2f", cost);
    }

    @Override
    public Double price() {
        return cost;
    }

    @Override
    public Object get() {
        return cost;
    }
}
