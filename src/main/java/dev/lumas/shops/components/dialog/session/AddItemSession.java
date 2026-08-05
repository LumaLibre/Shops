package dev.lumas.shops.components.dialog.session;

import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.Stock;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.interfaces.Product;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Getter
@Setter
@NullMarked
@Accessors(fluent = true)
public class AddItemSession {

    private final MarketTemplate market;
    private final ItemStack stack;
    private final Product product;
    private final Stock stock;

    @Nullable
    private Currency<? extends Number> currency;

    @Nullable
    private MarketItem editing;

    public AddItemSession(MarketTemplate market, ItemStack stack, Product product, Stock stock) {
        this.market = market;
        this.stack = stack;
        this.product = product;
        this.stock = stock;
    }

    public <T extends Currency<? extends Number>> @Nullable T editingCurrency(Class<T> type) {
        if (editing == null) return null;
        Currency<? extends Number> current = editing.currency();
        return type.isInstance(current) ? type.cast(current) : null;
    }

    public MarketItem build(Key key) {
        if (currency == null) throw new IllegalStateException("Currency not set");
        return new MarketItem(key, stock, currency, product, stack);
    }
}