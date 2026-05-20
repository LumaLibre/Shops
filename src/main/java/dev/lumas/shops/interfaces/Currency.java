package dev.lumas.shops.interfaces;

import dev.lumas.shops.constants.suppliers.Currencies;
import org.bukkit.entity.Player;

public interface Currency<T extends Number> extends EnumType<Currencies> {

    T getBalance(Player player);

    boolean withdraw(Player player, T amount);

    String price();

    default boolean hasEnough(Player player, T amount) {
        return getBalance(player).doubleValue() >= amount.doubleValue();
    }
}
