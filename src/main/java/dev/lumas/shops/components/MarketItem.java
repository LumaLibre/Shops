package dev.lumas.shops.components;

import com.google.common.base.Preconditions;
import dev.lumas.shops.components.data.PurchaseReceipt;
import dev.lumas.shops.components.data.Stock;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.constants.PurchaseResult;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.interfaces.Product;
import dev.lumas.shops.util.ClassUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static dev.lumas.shops.util.ItemStacks.addLines;

@Getter
@NullMarked
@AllArgsConstructor
@Accessors(fluent = true)
public class MarketItem implements Keyed {

    private transient final Key key;
    private final Stock stock;
    private final Currency<? extends Number> currency;
    private final Product product;
    @Getter(AccessLevel.NONE)
    private final ItemStack stack;

    public PurchaseResult purchase(Market market, Player player) {
        return purchase(market, player, 1);
    }

    public PurchaseResult purchase(Market market, Player player, int amount) {
        if (!currency.hasEnough(player, amount)) {
            return PurchaseResult.NOT_ENOUGH_CURRENCY;
        }
        PurchaseReceipt fingerPrint = PurchaseReceipt.of(player.getUniqueId(), key);

        if (stock.hasPlayerStock() && market.getPurchasesOf(fingerPrint) + amount > stock.player()) {
            return PurchaseResult.TOO_MANY_PURCHASES;
        } else if (stock.hasGlobalStock() && market.getGlobalPurchasesOf(key) + amount > stock.global()) {
            return PurchaseResult.NOT_ENOUGH_STOCK;
        }

        if (currency.withdraw(player, amount)) {
            market.addPurchase(fingerPrint, amount);
            market.refreshItem(this);
            product.give(player, this, amount);
        } else {
            throw new IllegalStateException("Currency withdraw failed");
        }
        return PurchaseResult.SUCCESS;
    }

    public ItemStack display(MarketState marketState, Locale locale) {
        // We have to rebuild the lore every time because stock may have changed.
        ItemStack stackCopy = stack();
        List<Component> lore = stackCopy.lore();
        List<Component> components = lore != null ? lore : new ArrayList<>();

        addLines(components, locale, "shops.gui.itemstack.description");
        addLines(components, locale, "shops.gui.itemstack.price", currency.readablePrice());
        if (stock.hasGlobalStock()) {
            int globalStock = stock.global();
            addLines(components, locale, "shops.gui.itemstack.stock", marketState.getRemainingGlobalStock(globalStock, key), globalStock);
        }

        stackCopy.lore(components);
        return stackCopy;
    }

    public Component displayName() {
        ItemStack stackCopy = stack();
        ItemMeta meta = stackCopy.getItemMeta();
        if (meta == null || !meta.hasCustomName()) {
            return Component.text(ClassUtil.formatEnum(stackCopy.getType()));
        }
        return Preconditions.checkNotNull(meta.customName(), "Item meta has no display name");
    }

    public ItemStack stack() {
        return stack.clone();
    }
}
