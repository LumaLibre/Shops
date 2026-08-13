package dev.lumas.shops.api.currency;

import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Every {@link CurrencyType} the plugin knows about, keyed by {@link CurrencyType#key()}.
 *
 * <p>The built-ins are registered on enable; third-party plugins add theirs from their own
 * {@code onEnable} (declare {@code depend: [Shops]} so the order is guaranteed). Iteration order
 * is registration order, which is also the order of the add-item dialog's currency dropdown.
 *
 * <p>Nothing removes a market that references a currency this registry doesn't have — reading one
 * throws {@link UnknownCurrencyException} and the market is skipped, so pulling a plugin out only
 * costs you the markets that priced items in its currency.
 */
@NullMarked
public final class CurrencyRegistry {

    public static final CurrencyRegistry INSTANCE = new CurrencyRegistry();

    private static final String DEFAULT_NAMESPACE = "shops";

    private final Map<Key, CurrencyType<?>> types = new LinkedHashMap<>();

    private CurrencyRegistry() {
    }

    /**
     * Registers a currency type.
     * @param type The type to register.
     * @throws IllegalStateException if another type is already registered under the same key.
     */
    public synchronized void register(CurrencyType<?> type) {
        CurrencyType<?> existing = types.putIfAbsent(type.key(), type);
        if (existing != null && existing != type) {
            throw new IllegalStateException("A currency type is already registered under " + type.key());
        }
    }

    /**
     * Removes a currency type. Markets already loaded keep working; markets read after this
     * point fail to load if they price anything in it.
     * @param key The key to unregister.
     * @return true if a type was removed.
     */
    public synchronized boolean unregister(Key key) {
        return types.remove(key) != null;
    }

    /**
     * @param key The key to look up.
     * @return The registered type, or {@code null} if there is none.
     */
    public synchronized @Nullable CurrencyType<?> get(Key key) {
        return types.get(key);
    }

    /**
     * Resolves an id as written in market JSON or picked in the add-item dialog. Ids without a
     * namespace are read as {@code shops:<id>}, which is what keeps pre-registry markets — where
     * currencies were serialized as {@code MONEY}, {@code ITEMSTACK}, {@code LUMAITEMS} — loading.
     *
     * @param id The raw id.
     * @return The registered type, or {@code null} if the id is unparseable or unregistered.
     */
    public @Nullable CurrencyType<?> resolve(String id) {
        Key key;
        try {
            key = id.indexOf(Key.DEFAULT_SEPARATOR) < 0
                    ? Key.key(DEFAULT_NAMESPACE, id.toLowerCase(Locale.ROOT))
                    : Key.key(id);
        } catch (Exception e) {
            return null;
        }
        return get(key);
    }

    /**
     * Resolves an id, refusing to carry on without one.
     * @param id The raw id, as {@link #resolve(String)} accepts it.
     * @return The registered type.
     * @throws UnknownCurrencyException if nothing is registered under {@code id}.
     */
    public CurrencyType<?> require(String id) {
        CurrencyType<?> type = resolve(id);
        if (type == null) {
            throw new UnknownCurrencyException(id);
        }
        return type;
    }

    /**
     * @return A snapshot of the registered types, in registration order.
     */
    public synchronized Collection<CurrencyType<?>> values() {
        return List.copyOf(types.values());
    }
}
