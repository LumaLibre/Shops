package dev.lumas.shops.api.currency;

import dev.lumas.shops.interfaces.Currency;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;

/**
 * A kind of currency markets can price items in, e.g. {@code shops:money}.
 *
 * <p>Types are registered with the {@link CurrencyRegistry} and looked up by {@link #key()}
 * when a market is read off disk, so the key is part of the on-disk format:
 *
 * <pre>{@code
 * "currency": { "type": "myplugin:gems", "value": 40 }
 * }</pre>
 *
 * <p>{@code value} is deserialized as {@link #amountType()} and handed to
 * {@link #create(Object)}. Register a type from another plugin's {@code onEnable}:
 *
 * <pre>{@code
 * ShopsAPI.getInstance().registerCurrency(
 *         CurrencyType.builder(Key.key("myplugin", "gems"), Integer.class)
 *                 .factory(amount -> new GemCurrency(amount == null ? 0 : amount))
 *                 .displayName(Component.text("Gems"))
 *                 .editor(new GemCurrencyEditor())
 *                 .build());
 * }</pre>
 *
 * @param <A> The amount type as it appears under {@code value} in market JSON.
 */
@NullMarked
public interface CurrencyType<A> extends Keyed {

    /**
     * The identifier written to (and read from) market JSON. Namespace it after the plugin
     * that owns the currency so it can't collide with another plugin's.
     * @return The key of this currency type.
     */
    @Override
    Key key();

    /**
     * The type the {@code value} field deserializes to. Anything Gson can handle works;
     * a record of your own is usually the cleanest option.
     * @return The amount type.
     */
    Class<A> amountType();

    /**
     * Builds a currency instance from a deserialized amount.
     * @param amount The deserialized {@code value}, or {@code null} when the field is absent.
     * @return The currency.
     */
    Currency<? extends Number> create(@Nullable A amount);

    /**
     * The label shown in the add-item dialog's currency dropdown.
     * @param locale The viewer's locale.
     * @return The display name.
     */
    Component displayName(Locale locale);

    /**
     * The flow that asks an admin for a price in this currency.
     * @return The editor.
     */
    CurrencyEditor editor();

    /**
     * Starts building a currency type.
     * @param key        The identifier, namespaced after the owning plugin.
     * @param amountType The type the {@code value} field deserializes to.
     * @return A new builder.
     */
    static <A> Builder<A> builder(Key key, Class<A> amountType) {
        return new Builder<>(key, amountType);
    }

    /**
     * Builder for the common case: a factory, a label, and an editor.
     * @param <A> The amount type.
     */
    final class Builder<A> {

        private final Key key;
        private final Class<A> amountType;
        private Function<Locale, Component> displayName;
        private CurrencyEditor editor = CurrencyEditor.unsupported();
        private @Nullable Function<@Nullable A, Currency<? extends Number>> factory;

        private Builder(Key key, Class<A> amountType) {
            this.key = key;
            this.amountType = amountType;
            this.displayName = _ -> Component.text(key.value());
        }

        /**
         * Sets how a deserialized amount becomes a currency. Required.
         * @param factory The factory. Receives {@code null} when the JSON has no {@code value}.
         * @return This builder.
         */
        public Builder<A> factory(Function<@Nullable A, Currency<? extends Number>> factory) {
            this.factory = factory;
            return this;
        }

        /**
         * Sets a fixed dropdown label.
         * @param displayName The label.
         * @return This builder.
         */
        public Builder<A> displayName(Component displayName) {
            this.displayName = _ -> displayName;
            return this;
        }

        /**
         * Sets a dropdown label rendered per-viewer through Adventure's global translator.
         * @param translationKey The translation key.
         * @return This builder.
         */
        public Builder<A> translation(String translationKey) {
            this.displayName = locale -> GlobalTranslator.render(Component.translatable(translationKey), locale);
            return this;
        }

        /**
         * Sets the admin-facing price editor. Defaults to {@link CurrencyEditor#unsupported()},
         * which tells admins the currency has to be configured in JSON by hand.
         * @param editor The editor.
         * @return This builder.
         */
        public Builder<A> editor(CurrencyEditor editor) {
            this.editor = editor;
            return this;
        }

        /**
         * @return The built currency type.
         * @throws NullPointerException if no factory was set.
         */
        public CurrencyType<A> build() {
            return new CurrencyTypeImpl<>(
                    key,
                    amountType,
                    Objects.requireNonNull(factory, "factory"),
                    displayName,
                    editor
            );
        }
    }
}
