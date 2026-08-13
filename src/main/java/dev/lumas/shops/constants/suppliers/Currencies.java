package dev.lumas.shops.constants.suppliers;

import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.api.currency.CurrencyRegistry;
import dev.lumas.shops.api.currency.CurrencyType;
import dev.lumas.shops.components.currency.ItemStackCurrencyImpl;
import dev.lumas.shops.components.currency.LumaItemsCurrencyImpl;
import dev.lumas.shops.components.currency.MoneyCurrencyImpl;
import dev.lumas.shops.components.dialog.session.LumaItemCurrencyDialog;
import dev.lumas.shops.components.dialog.session.MoneyCurrencyDialog;
import dev.lumas.shops.listeners.ItemStackPickListener;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.jspecify.annotations.NullMarked;

/**
 * The currency types Shops ships with.
 *
 * <p>Their keys double as the pre-registry serialized names ({@code MONEY} reads back as
 * {@code shops:money}), so markets written before currencies became a registry still load.
 */
@NullMarked
@UtilityClass
public final class Currencies {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    private static final String LUMAITEMS_PLUGIN = "LumaItems";

    /** Vault-backed balance. */
    public static final CurrencyType<Double> MONEY = CurrencyType.builder(Key.key("shops", "money"), Double.class)
            .translation("shops.additem.currency.money")
            .factory(cost -> new MoneyCurrencyImpl(cost == null ? 0 : cost))
            .editor((player, selection, onComplete, _) ->
                    new MoneyCurrencyDialog(player.locale(), selection, onComplete).show(player))
            .build();

    /** A stack of vanilla items out of the buyer's inventory. */
    public static final CurrencyType<ItemStackCurrencyImpl.ItemStackAmount> ITEMSTACK =
            CurrencyType.builder(Key.key("shops", "itemstack"), ItemStackCurrencyImpl.ItemStackAmount.class)
                    .translation("shops.additem.currency.itemstack")
                    .factory(ItemStackCurrencyImpl::new)
                    .editor((player, selection, onComplete, onCancel) ->
                            ItemStackPickListener.INSTANCE.begin(selection, player, onComplete, onCancel))
                    .build();

    /** A custom item from LumaItems. Only registered when that plugin is installed. */
    public static final CurrencyType<LumaItemsCurrencyImpl.LumaItemsAmount> LUMAITEMS =
            CurrencyType.builder(Key.key("shops", "lumaitems"), LumaItemsCurrencyImpl.LumaItemsAmount.class)
                    .translation("shops.additem.currency.lumaitems")
                    .factory(LumaItemsCurrencyImpl::new)
                    .editor((player, selection, onComplete, _) ->
                            new LumaItemCurrencyDialog(player.locale(), selection, onComplete).show(player))
                    .build();

    /**
     * Registers the built-ins. Called on enable, before anything can read a market.
     */
    public static void registerDefaults() {
        CurrencyRegistry registry = CurrencyRegistry.INSTANCE;
        registry.register(MONEY);
        registry.register(ITEMSTACK);

        // Soft dependency: without it every LumaItems-priced market fails to load, which beats
        // handing players a price the server can't collect.
        if (Bukkit.getPluginManager().getPlugin(LUMAITEMS_PLUGIN) != null) {
            registry.register(LUMAITEMS);
        } else {
            LOGGER.info("LumaItems is not installed, skipping the " + LUMAITEMS.key() + " currency");
        }
    }
}
