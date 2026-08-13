package dev.lumas.shops.components.dialog;

import com.google.common.base.Preconditions;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.api.currency.CurrencyRegistry;
import dev.lumas.shops.api.currency.CurrencyType;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.components.data.Stock;
import dev.lumas.shops.components.dialog.session.AddItemSession;
import dev.lumas.shops.components.product.CommandProductImpl;
import dev.lumas.shops.components.product.LumaItemsProductImpl;
import dev.lumas.shops.components.product.ShopItemProductImpl;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.config.ShopsConfig;
import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.Product;
import dev.lumas.shops.interfaces.ShopsDialog;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.Numbers;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.input.SingleOptionDialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@NullMarked
@Accessors(fluent = true)
@SuppressWarnings({"UnstableApiUsage", "PatternValidation"})
public class AddMarketItemDialog extends ShopsDialog {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    private static final String INPUT_CURRENCY = "currency_type";
    private static final String INPUT_PRODUCT_TYPE = "product_type";
    private static final String INPUT_PRODUCT_VALUE = "product_value";
    private static final String INPUT_PLAYER_STOCK = "player_stock";
    private static final String INPUT_GLOBAL_STOCK = "global_stock";
    private static final String INPUT_INDEX = "index";

    private final KeyConsumer<AddMarketItemDialog> submit = KeyConsumer.of(
            this,
            Key.key("shops:additem/submit"),
            AddMarketItemDialog::onSubmit
    );

    private final MarketTemplate market;
    private final ItemStack itemToAdd;

    /**
     * When set, the dialog edits this item in place — every input starts on its current
     * value and submitting replaces it instead of adding a new item. Its key is kept so
     * purchase history stays attached.
     */
    @Setter
    private @Nullable MarketItem editing;

    public AddMarketItemDialog(Locale locale, MarketTemplate market, ItemStack itemToAdd) {
        super(locale);
        this.market = market;
        this.itemToAdd = itemToAdd;
    }

    @Override
    public Dialog build() {
        CurrencyType<?> initialCurrency = editing == null ? Currencies.MONEY : editing.currency().type();
        Products initialProduct = editing == null ? Products.SHOP_ITEM : editing.product().type();

        SingleOptionDialogInput currencyInput = DialogInput.singleOption(
                INPUT_CURRENCY,
                translate("shops.additem.input.currency"),
                CurrencyRegistry.INSTANCE.values().stream()
                        .map(currency -> SingleOptionDialogInput.OptionEntry.create(
                                currency.key().asString(),
                                currency.displayName(locale()),
                                currency.equals(initialCurrency)
                        ))
                        .toList()
        ).width(300).build();

        SingleOptionDialogInput productInput = DialogInput.singleOption(
                INPUT_PRODUCT_TYPE,
                translate("shops.additem.input.product"),
                Arrays.stream(Products.values())
                        .map(products -> SingleOptionDialogInput.OptionEntry.create(
                                products.name(),
                                translate("shops.additem.product." + products.name().toLowerCase(Locale.ROOT)),
                                products == initialProduct
                        ))
                        .toList()
        ).width(300).build();

        DialogInput productValueInput = DialogInput.text(INPUT_PRODUCT_VALUE, translate("shops.additem.input.product_value"))
                .maxLength(412)
                .initial(initialProductValue())
                .width(300)
                .build();

        DialogInput playerStockInput = DialogInput.text(INPUT_PLAYER_STOCK, translate("shops.additem.input.player_stock"))
                .maxLength(8)
                .initial(editing == null ? "-1" : String.valueOf(editing.stock().player()))
                .width(200)
                .build();

        DialogInput globalStockInput = DialogInput.text(INPUT_GLOBAL_STOCK, translate("shops.additem.input.global_stock"))
                .maxLength(10)
                .initial(editing == null ? "-1" : String.valueOf(editing.stock().global()))
                .width(200)
                .build();

        ActionButton continueButton = ActionButton.builder(translate("shops.additem.button.continue"))
                .action(DialogAction.customClick(submit.key(), null))
                .build();

        ActionButton cancelButton = ActionButton.builder(translate("shops.additem.button.cancel"))
                .action(null)
                .build();

        int itemCount = market.items().size();
        // Adding: range is [0, itemCount]. itemCount is "append at the end" (100% on the slider).
        // If the market is empty, max == 0 and the slider is at a single point.
        // Editing: the item already occupies a slot, so the last valid position is itemCount - 1.
        float max = editing == null ? (float) itemCount : (float) (itemCount - 1);
        float initialIndex = editing == null ? max : Math.max(0f, market.indexOf(editing.key()));
        DialogInput indexInput = DialogInput.numberRange(
                INPUT_INDEX,
                translate("shops.additem.input.index"),
                0f,
                max
        ).step(1f).initial(initialIndex).labelFormat("%s: %s").width(300).build();


        DialogBase base = DialogBase.builder(translate(editing == null ? "shops.additem.title" : "shops.edititem.title"))
                .canCloseWithEscape(false)
                .body(List.of(
                        DialogBody.item(itemToAdd)
                                .description(DialogBody.plainMessage(translate(editing == null ? "shops.additem.description" : "shops.edititem.description")))
                                .build()
                ))
                .inputs(List.of(currencyInput, productInput, productValueInput, playerStockInput, globalStockInput, indexInput))
                .build();

        return Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(continueButton, cancelButton))
        );
    }

    private void onSubmit(Player player, DialogResponseView view) {
        String currencyName = view.getText(INPUT_CURRENCY);
        String productName = view.getText(INPUT_PRODUCT_TYPE);
        String productValue = view.getText(INPUT_PRODUCT_VALUE);
        int playerStock = Numbers.parseInt(view.getText(INPUT_PLAYER_STOCK), -1);
        int globalStock = Numbers.parseInt(view.getText(INPUT_GLOBAL_STOCK), -1);
        // Falls back to appending, or to the item's current position when editing.
        float fallbackIndex = editing == null ? (float) market.items().size() : Math.max(0f, market.indexOf(editing.key()));
        int index = Math.round(Numbers.unbox(view.getFloat(INPUT_INDEX), fallbackIndex));

        if (currencyName == null || productName == null) {
            Viewers.sendMessage(player, "shops.additem.error.incomplete");
            return;
        }

        CurrencyType<?> currencyType = CurrencyRegistry.INSTANCE.resolve(currencyName);
        if (currencyType == null) {
            // The plugin owning that currency went away between opening the dialog and submitting it.
            Viewers.sendMessage(player, "shops.additem.error.unknown_currency", currencyName);
            return;
        }
        Products productType = Products.valueOf(productName);

        Product product = buildProduct(productType, productValue);
        if (product == null) {
            Viewers.sendMessage(player, "shops.additem.error.bad_product");
            return;
        }

        AddItemSession session = new AddItemSession(market, itemToAdd, product, new Stock(playerStock, globalStock));
        session.editing(editing);

        // Dispatch into the currency's own editor. When complete, finish() is called with the
        // populated session. Cancelling drops the player back here so they can pick another currency.
        currencyType.editor().open(player, session, () -> finish(player, session, index), () -> show(player));
    }

    private String initialProductValue() {
        if (editing == null) return "";
        Object value = editing.product().get();
        return value == null ? "" : value.toString();
    }

    private @Nullable Product buildProduct(Products type, @org.jspecify.annotations.Nullable String value) {
        return switch (type) {
            case SHOP_ITEM -> new ShopItemProductImpl();
            case COMMAND -> {
                if (value == null || value.isBlank()) yield null;
                yield new CommandProductImpl(value);
            }
            case LUMAITEMS -> {
                if (value == null || value.isBlank()) yield null;
                yield new LumaItemsProductImpl(value);
            }
        };
    }

    private void finish(Player player, AddItemSession session, int index) {
        MarketItem edited = editing;
        Key key = edited != null ? edited.key() : deriveItemKey(session);
        MarketItem item = session.build(key);
        try {
            Key marketKey = session.market().key();
            if (edited != null) {
                MarketManager.INSTANCE.replaceItem(marketKey, item, index);
                Viewers.sendMessage(player, "shops.edititem.success", key);
            } else {
                MarketManager.INSTANCE.addItem(marketKey, item, index);
                Viewers.sendMessage(player, "shops.additem.success", key);
            }

            if (ShopsConfig.instance().openAfterAddingItem()) {
                Market newMarket = MarketManager.INSTANCE.market(marketKey, player.locale());
                Preconditions.checkNotNull(newMarket, "Market should not be null");
                newMarket.open(player);
                newMarket.setPage(item);
            }
        } catch (Exception e) {
            Viewers.sendMessage(player, "shops.additem.error.save", e.toString());
            throw e;
        }
    }

    private static Key deriveItemKey(AddItemSession session) {
        Key marketKey = session.market().key();
        String name = extractItemName(session.stack());
        String basePath = marketKey.value() + "/" + name;
        String namespace = marketKey.namespace();

        Map<Key, MarketItem> existing = session.market().items();
        if (ShopsConfig.instance().debug()) {
            LOGGER.info("Existing items: " + existing.keySet());
        }
        Key candidate = Key.key(namespace, basePath);
        int suffix = 2;
        while (existing.containsKey(candidate)) {
            candidate = Key.key(namespace, basePath + suffix);
            suffix++;
        }
        return candidate;
    }

    private static String extractItemName(ItemStack stack) {
        if (stack.getItemMeta() != null && stack.getItemMeta().hasCustomName()) {
            String text = PlainTextComponentSerializer.plainText().serialize(stack.getItemMeta().customName());
            return text.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+", "_").replaceAll("^_+|_+$", "");
        }
        return stack.getType().name().toLowerCase(Locale.ROOT);
    }

    @Override
    public void show(Player player) {
        KeyConsumerRegistry.INSTANCE.register(player, submit);
        player.showDialog(build());
    }
}