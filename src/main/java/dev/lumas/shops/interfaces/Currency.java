package dev.lumas.shops.interfaces;

import dev.lumas.shops.api.currency.CurrencyType;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

/**
 * Represents a currency.
 * A currency is a type of item, physical or digital, that can be used to purchase items.
 * @param <T> The type of the currency.
 */
public interface Currency<T extends Number> extends Accessor<Object> {

    /**
     * The registered type this currency was created from. Its key is what gets written to
     * market JSON, and what the currency is looked up by when the market is read back.
     * @return The currency type.
     */
    CurrencyType<?> type();

    /**
     * The amount this currency was built from, written back out as the {@code value} field of
     * the market's JSON. It has to be an instance of {@link CurrencyType#amountType()} — the
     * same object {@link CurrencyType#create(Object)} would take to rebuild this currency.
     *
     * @return The amount, or {@code null} to leave {@code value} out of the JSON entirely.
     */
    @Override
    @Nullable Object get();

    /**
     * Gets the price of the currency.
     * @return The price of the currency.
     */
    T price();

    /**
     * Gets the balance of the currency for a player.
     * @param player The player to get the balance for.
     * @return The balance of the currency.
     */
    T getBalance(Player player);

    /**
     * Withdraws the currency from the player's inventory.
     * @param player The player to withdraw from.
     * @param multiplier The amount to multiply the price by.
     * @return True if the currency was withdrawn successfully, false otherwise.
     */
    boolean withdraw(Player player, int multiplier);

    default boolean withdraw(Player player) {
        return withdraw(player, 1);
    }

    /**
     * Gets the price of the currency in a readable format.
     * @return The price of the currency in a readable format.
     */
    Component readablePrice(int multiplier);

    default Component readablePrice() {
        return readablePrice(1);
    }

    /**
     * Checks if the player has enough currency to purchase the item.
     * @param player The player to check.
     * @return True if the player has enough currency, false otherwise.
     */
    default boolean hasEnough(Player player, int multiplier) {
        return getBalance(player).doubleValue() >= (price().doubleValue() * multiplier);
    }
}
