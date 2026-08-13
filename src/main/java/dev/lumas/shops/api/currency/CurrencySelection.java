package dev.lumas.shops.api.currency;

import dev.lumas.shops.interfaces.Currency;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * The half-built market item a {@link CurrencyEditor} writes its result into.
 *
 * <p>An editor reads {@link #editingCurrency()} to prefill its inputs, then calls
 * {@link #currency(Currency)} once the admin has picked a price.
 */
@NullMarked
public interface CurrencySelection {

    /**
     * The currency already attached to the item being edited.
     * @return the current currency, or {@code null} when a brand-new item is being added.
     */
    @Nullable Currency<? extends Number> editingCurrency();

    /**
     * Narrowed variant of {@link #editingCurrency()}.
     * @param type The implementation the editor knows how to prefill from.
     * @return the current currency, or {@code null} when the item is new or priced in another currency.
     */
    default <T extends Currency<? extends Number>> @Nullable T editingCurrency(Class<T> type) {
        Currency<? extends Number> current = editingCurrency();
        return type.isInstance(current) ? type.cast(current) : null;
    }

    /**
     * Sets the currency the item will be priced in.
     * @param currency The chosen currency.
     */
    void currency(Currency<? extends Number> currency);
}
