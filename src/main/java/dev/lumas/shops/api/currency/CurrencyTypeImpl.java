package dev.lumas.shops.api.currency;

import dev.lumas.shops.interfaces.Currency;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.function.Function;

@NullMarked
record CurrencyTypeImpl<A>(
        Key key,
        Class<A> amountType,
        Function<@Nullable A, Currency<? extends Number>> factory,
        Function<Locale, Component> displayName,
        CurrencyEditor editor
) implements CurrencyType<A> {

    @Override
    public Currency<? extends Number> create(@Nullable A amount) {
        return factory.apply(amount);
    }

    @Override
    public Component displayName(Locale locale) {
        return displayName.apply(locale);
    }
}
