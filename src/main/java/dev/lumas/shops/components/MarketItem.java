package dev.lumas.shops.components;

import dev.lumas.shops.components.serial.SerialStack;
import dev.lumas.shops.interfaces.Currency;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@Getter
@NullMarked
@AllArgsConstructor
@Accessors(fluent = true)
public class MarketItem implements Keyed {

    private final SerialStack stack;
    private final Currency<Number> currency;
    private final Number cost;
    private final int playerStock; // How many times a single player can purchase this item
    private final int globalStock; // How many times this item can be purchased globally

    private int purchases;


    public PurchaseResult purchase(MarketPlayer marketPlayer) {
        Player player = marketPlayer.getPlayerOrThrow();
        if (!currency.hasEnough(player, cost)) {
            return PurchaseResult.NOT_ENOUGH_CURRENCY;
        }

        Key key = this.key();
        // TODO: Maybe should be -1?
        if (playerStock > 0 && marketPlayer.getPurchasesOf(key) >= playerStock) {
            return PurchaseResult.TOO_MANY_PURCHASES;
        } else if (globalStock > 0 && purchases + 1 > globalStock) {
            return PurchaseResult.NOT_ENOUGH_STOCK;
        }

        if (currency.withdraw(player, cost)) {
            // TODO: Figure out how to give items
            marketPlayer.addPurchase(key);
            purchases++;
        } else {
            throw new IllegalStateException("Currency withdraw failed");
        }
        return PurchaseResult.SUCCESS;
    }

    @Override
    public Key key() {
        return stack.key();
    }


    public enum PurchaseResult {
        SUCCESS,
        NOT_ENOUGH_CURRENCY,
        NOT_ENOUGH_STOCK,
        TOO_MANY_PURCHASES
    }
}
