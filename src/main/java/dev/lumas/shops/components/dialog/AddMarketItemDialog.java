package dev.lumas.shops.components.dialog;

import com.google.common.base.Preconditions;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.components.data.Stock;
import dev.lumas.shops.components.dialog.session.AddItemSession;
import dev.lumas.shops.components.dialog.session.CurrencyFlow;
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

    public AddMarketItemDialog(Locale locale, MarketTemplate market, ItemStack itemToAdd) {
        super(locale);
        this.market = market;
        this.itemToAdd = itemToAdd;
    }

    @Override
    public Dialog build() {
        SingleOptionDialogInput currencyInput = DialogInput.singleOption(
                INPUT_CURRENCY,
                translate("shops.additem.input.currency"),
                Arrays.stream(Currencies.values())
                        .map(currencies -> SingleOptionDialogInput.OptionEntry.create(
                                currencies.name(),
                                translate("shops.additem.currency." + currencies.name().toLowerCase(Locale.ROOT)),
                                currencies == Currencies.MONEY
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
                                products == Products.SHOP_ITEM
                        ))
                        .toList()
        ).width(300).build();

        DialogInput productValueInput = DialogInput.text(INPUT_PRODUCT_VALUE, translate("shops.additem.input.product_value"))
                .maxLength(412)
                .width(300)
                .build();

        DialogInput playerStockInput = DialogInput.text(INPUT_PLAYER_STOCK, translate("shops.additem.input.player_stock"))
                .maxLength(8)
                .initial("-1")
                .width(200)
                .build();

        DialogInput globalStockInput = DialogInput.text(INPUT_GLOBAL_STOCK, translate("shops.additem.input.global_stock"))
                .maxLength(10)
                .initial("-1")
                .width(200)
                .build();

        ActionButton continueButton = ActionButton.builder(translate("shops.additem.button.continue"))
                .action(DialogAction.customClick(submit.key(), null))
                .build();

        ActionButton cancelButton = ActionButton.builder(translate("shops.additem.button.cancel"))
                .action(null)
                .build();

        int itemCount = market.items().size();
        // Range is [0, itemCount]. itemCount is "append at the end" (100% on the slider).
        // If the market is empty, max == 0 and the slider is at a single point.
        float max = (float) itemCount;
        DialogInput indexInput = DialogInput.numberRange(
                INPUT_INDEX,
                translate("shops.additem.input.index"),
                0f,
                max
        ).step(1f).initial(max).labelFormat("%s: %s").width(300).build();


        DialogBase base = DialogBase.builder(translate("shops.additem.title"))
                .canCloseWithEscape(false)
                .body(List.of(
                        DialogBody.item(itemToAdd)
                                .description(DialogBody.plainMessage(translate("shops.additem.description")))
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
        int index = Math.round(Numbers.unbox(view.getFloat(INPUT_INDEX), (float) market.items().size()));

        if (currencyName == null || productName == null) {
            Viewers.sendMessage(player, "shops.additem.error.incomplete");
            return;
        }

        Currencies currencyType = Currencies.valueOf(currencyName);
        Products productType = Products.valueOf(productName);

        Product product = buildProduct(productType, productValue);
        if (product == null) {
            Viewers.sendMessage(player, "shops.additem.error.bad_product");
            return;
        }

        AddItemSession session = new AddItemSession(market, itemToAdd, product, new Stock(playerStock, globalStock));

        // Dispatch into currency sub-flow. When complete, finish() is called with the populated session.
        CurrencyFlow.start(currencyType, session, player, () -> finish(player, session, index));
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
        Key key = deriveItemKey(session);
        MarketItem item = session.build(key);
        try {
            Key marketKey = session.market().key();
            MarketManager.INSTANCE.addItem(marketKey, item, index);
            Viewers.sendMessage(player, "shops.additem.success", key);

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