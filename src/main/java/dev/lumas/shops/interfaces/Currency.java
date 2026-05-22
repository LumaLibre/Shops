package dev.lumas.shops.interfaces;

import dev.lumas.shops.constants.suppliers.Currencies;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

public interface Currency<T extends Number> extends EnumType<Currencies> {

    T getBalance(Player player);

    boolean withdraw(Player player);

    Component readablePrice();

    T price();

    default boolean hasEnough(Player player) {
        return getBalance(player).doubleValue() >= price().doubleValue();
    }
}
