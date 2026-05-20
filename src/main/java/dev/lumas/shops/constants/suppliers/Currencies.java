package dev.lumas.shops.constants.suppliers;

import dev.lumas.shops.components.currency.ItemStackCurrencyImpl;
import dev.lumas.shops.components.currency.LumaItemsCurrencyImpl;
import dev.lumas.shops.components.currency.MoneyCurrencyImpl;
import dev.lumas.shops.components.data.TypedEntry;
import dev.lumas.shops.interfaces.Currency;

public enum Currencies {
    MONEY(new TypedEntry<>(Double.class, MoneyCurrencyImpl::new)),
    LUMAITEMS(new TypedEntry<>(LumaItemsCurrencyImpl.LumaItemsAmount.class, LumaItemsCurrencyImpl::new)),
    ITEMSTACK(new TypedEntry<>(ItemStackCurrencyImpl.ItemStackAmount.class, ItemStackCurrencyImpl::new));

    private final TypedEntry<?, ? extends Currency<?>> entry;

    Currencies(TypedEntry<?, ? extends Currency<?>> entry) {
        this.entry = entry;
    }

    public Currency<?> create(Object value) {
        return entry.create(value);
    }

    public Class<?> amountType() {
        return entry.type();
    }
}