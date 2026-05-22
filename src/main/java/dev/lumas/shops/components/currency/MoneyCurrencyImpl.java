package dev.lumas.shops.components.currency;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.util.Lazy;
import net.kyori.adventure.text.Component;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public record MoneyCurrencyImpl(double cost) implements Currency<Double> {

    private static final Lazy<Object> ECONOMY = Lazy.of(MoneyCurrencyImpl::loadEconomy);

    private static Object loadEconomy() {
        RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp =
                Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (rsp == null) {
            throw new IllegalStateException("No Economy provider registered (is Vault installed?)");
        }
        return rsp.getProvider();
    }

    private static net.milkbowl.vault.economy.Economy economy() {
        return (net.milkbowl.vault.economy.Economy) ECONOMY.get();
    }

    @Override
    public Double getBalance(Player player) {
        return economy().getBalance(player);
    }

    @Override
    public boolean withdraw(Player player) {
        EconomyResponse response = economy().withdrawPlayer(player, cost);
        return response.transactionSuccess();
    }

    @Override
    public Currencies type() {
        return Currencies.MONEY;
    }

    @Override
    public Component readablePrice() {
        return Component.text("$" + String.format("%.2f", cost));
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
