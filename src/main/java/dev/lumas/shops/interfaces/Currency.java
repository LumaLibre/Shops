package dev.lumas.shops.interfaces;

import dev.lumas.shops.constants.suppliers.Currencies;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Represents a currency.
 * A currency is a type of item, physical or digital, that can be used to purchase items.
 * @param <T> The type of the currency.
 */
public interface Currency<T extends Number> extends EnumType<Currencies> {

    /**
     * Gets the balance of the currency for a player.
     * @param player The player to get the balance for.
     * @return The balance of the currency.
     */
    T getBalance(Player player);

    /**
     * Withdraws the currency from the player's inventory.
     * @param player The player to withdraw from.
     * @return True if the currency was withdrawn successfully, false otherwise.
     */
    boolean withdraw(Player player);

    /**
     * Gets the price of the currency in a readable format.
     * @return The price of the currency in a readable format.
     */
    Component readablePrice();

    /**
     * Gets the price of the currency.
     * @return The price of the currency.
     */
    T price();

    /**
     * Checks if the player has enough currency to purchase the item.
     * @param player The player to check.
     * @return True if the player has enough currency, false otherwise.
     */
    default boolean hasEnough(Player player) {
        return getBalance(player).doubleValue() >= price().doubleValue();
    }
}
