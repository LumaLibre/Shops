package dev.lumas.shops.api;

import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.api.currency.CurrencyRegistry;
import dev.lumas.shops.api.currency.CurrencyType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;

/**
 * Entry point for other plugins.
 *
 * <p>Shops may be reloaded underneath you, so grab a fresh instance rather than caching one.
 *
 * <pre>{@code
 * ShopsAPI.getInstance().registerCurrency(
 *         CurrencyType.builder(Key.key("myplugin", "gems"), Integer.class)
 *                 .factory(amount -> new GemCurrency(amount == null ? 0 : amount))
 *                 .displayName(Component.text("Gems"))
 *                 .editor(new GemCurrencyEditor())
 *                 .build());
 * }</pre>
 */
@NullMarked
public final class ShopsAPI {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    private static @Nullable ShopsAPI singleton;

    private ShopsAPI() {
    }

    /**
     * @return The API instance.
     */
    public static synchronized ShopsAPI getInstance() {
        if (singleton == null) {
            singleton = new ShopsAPI();
            LOGGER.info("A plugin is accessing the Shops API. Creating a new instance! <gray>(Hash: " + singleton.hashCode() + ")");
        }
        return singleton;
    }

    /**
     * @return The registry backing every currency markets can price items in.
     */
    public CurrencyRegistry currencies() {
        return CurrencyRegistry.INSTANCE;
    }

    /**
     * Registers a currency type. Call this from your plugin's {@code onEnable}, every startup —
     * markets that reference a currency nobody registered are skipped when they're read.
     *
     * @param type The type to register.
     * @throws IllegalStateException if another type is already registered under the same key.
     */
    public void registerCurrency(CurrencyType<?> type) {
        CurrencyRegistry.INSTANCE.register(type);
    }

    /**
     * @param key The key to look up.
     * @return The registered currency type, or {@code null} if there is none.
     */
    public @Nullable CurrencyType<?> currency(Key key) {
        return CurrencyRegistry.INSTANCE.get(key);
    }

    /**
     * @return A snapshot of every registered currency type, in registration order.
     */
    public Collection<CurrencyType<?>> currencyTypes() {
        return CurrencyRegistry.INSTANCE.values();
    }
}
