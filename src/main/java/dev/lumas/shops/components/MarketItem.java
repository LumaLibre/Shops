package dev.lumas.shops.components;

import dev.lumas.shops.components.data.PurchaseFingerPrint;
import dev.lumas.shops.constants.PurchaseResult;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.interfaces.Product;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@Getter
@NullMarked
@AllArgsConstructor
@Accessors(fluent = true)
public class MarketItem implements Keyed {

    private final Key key;
    private final ItemStack stack;
    private final Currency<Number> currency;
    private final Product product;


    private final Number cost;
    private final int playerStock; // How many times a single player can purchase this item
    private final int globalStock; // How many times this item can be purchased globally

    private int purchases;


    public PurchaseResult purchase(Market market, MarketPlayer marketPlayer) {
        Player player = marketPlayer.getPlayerOrThrow();
        if (!currency.hasEnough(player, cost)) {
            return PurchaseResult.NOT_ENOUGH_CURRENCY;
        }
        PurchaseFingerPrint fingerPrint = PurchaseFingerPrint.of(market.key(), key);
        // TODO: Maybe should be -1?
        if (playerStock > 0 && marketPlayer.getPurchasesOf(fingerPrint) >= playerStock) {
            return PurchaseResult.TOO_MANY_PURCHASES;
        } else if (globalStock > 0 && purchases + 1 > globalStock) {
            return PurchaseResult.NOT_ENOUGH_STOCK;
        }

        if (currency.withdraw(player, cost)) {
            product.give(player, 1);
            marketPlayer.addPurchase(fingerPrint);
            purchases++;
        } else {
            throw new IllegalStateException("Currency withdraw failed");
        }
        return PurchaseResult.SUCCESS;
    }

    @Override
    public Key key() {
        return key;
    }

}
