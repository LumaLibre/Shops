package dev.lumas.shops.components;

import dev.lumas.shops.components.data.PurchaseReceipt;
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
    private final Currency<Number> currency;
    private final Product product;

    private final int playerStock; // How many times a single player can purchase this item
    private final int globalStock; // How many times this item can be purchased globally

    private final ItemStack stack;


    public PurchaseResult purchase(Market market, Player player) {
        if (!currency.hasEnough(player)) {
            return PurchaseResult.NOT_ENOUGH_CURRENCY;
        }
        PurchaseReceipt fingerPrint = PurchaseReceipt.of(player.getUniqueId(), key);

        if (playerStock > 0 && market.getPurchasesOf(fingerPrint) >= playerStock) {
            return PurchaseResult.TOO_MANY_PURCHASES;
        } else if (globalStock > 0 && market.getGlobalPurchasesOf(key) + 1 > globalStock) {
            return PurchaseResult.NOT_ENOUGH_STOCK;
        }

        if (currency.withdraw(player)) {
            market.addPurchase(fingerPrint);
            product.give(player, 1);
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
