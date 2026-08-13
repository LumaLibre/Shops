package dev.lumas.shops.api.currency;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.jspecify.annotations.NullMarked;

/**
 * Thrown while reading a market whose items are priced in a currency no plugin has registered —
 * usually because the plugin that owned that currency is no longer installed.
 *
 * <p>Caught by the market manager, which logs the offending id and skips the market rather than
 * loading it with a broken price.
 */
@Getter
@NullMarked
@Accessors(fluent = true)
public class UnknownCurrencyException extends RuntimeException {

    /** The unresolvable id, exactly as it appeared in the market's JSON. */
    private final String currencyId;

    public UnknownCurrencyException(String currencyId) {
        super("No currency type is registered under '" + currencyId + "'");
        this.currencyId = currencyId;
    }
}
