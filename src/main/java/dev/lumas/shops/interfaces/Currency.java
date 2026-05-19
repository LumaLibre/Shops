package dev.lumas.shops.interfaces;

import org.bukkit.entity.Player;

public interface Currency<T extends Number> {

    T getBalance(Player player);

    boolean withdraw(Player player, T amount);

    default boolean hasEnough(Player player, T amount) {
        return getBalance(player).doubleValue() >= amount.doubleValue();
    }
}
