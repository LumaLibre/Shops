package dev.lumas.shops.components.dialog;

import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.components.data.PurchaseReceipt;
import dev.lumas.shops.config.ShopsConfig;
import dev.lumas.shops.constants.PurchaseResult;
import dev.lumas.shops.interfaces.ShopsDialog;
import dev.lumas.shops.util.CollectionUtil;
import dev.lumas.shops.util.Scheduling;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class ConfirmationDialog extends ShopsDialog {

    private final KeyConsumer<ConfirmationDialog> yes = KeyConsumer.of(
            this,
            Key.key("shops:confirm/yes"),
            ConfirmationDialog::onConfirm
    );
    private final KeyConsumer<ConfirmationDialog> no = KeyConsumer.of(
            this,
            Key.key("shops:confirm/no"),
            (_, _, _) -> {}
    );

    private final Player player;
    private final Market market;
    private final MarketItem marketItem;

    public ConfirmationDialog(Player player, Market market, MarketItem marketItem) {
        super(player.locale());
        this.player = player;
        this.market = market;
        this.marketItem = marketItem;
    }

    @Override
    public Dialog build() {
        ItemStack itemStack = marketItem.stack();
        Component price = marketItem.currency().readablePrice();
        int remainingStock = market.state().getRemainingStock(marketItem.stock().player(), PurchaseReceipt.of(player.getUniqueId(), marketItem.key()));

        DialogBody body = DialogBody.item(itemStack)
                .description(DialogBody.plainMessage(translate("shops.confirm.content", price)))
                .build();

        DialogBody body2 = marketItem.stock().hasPlayerStock() ? DialogBody.plainMessage(translate("shops.confirm.player_stock", Component.text(remainingStock))) : null;


        ActionButton yesButton = ActionButton.builder(translate("shops.confirm.button.yes"))
                .action(DialogAction.customClick(yes.key(), null))
                .build();

        ActionButton noButton = ActionButton.builder(translate("shops.confirm.button.no"))
                .action(DialogAction.customClick(no.key(), null))
                .build();

        DialogBase base = DialogBase.builder(translate("shops.confirm.title"))
                .canCloseWithEscape(true)
                .body(CollectionUtil.ofNonNull(body, body2))
                .build();

        return Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(yesButton, noButton))
        );
    }

    @Override
    public void show(Player player) {
        KeyConsumerRegistry.INSTANCE.register(player, yes, no);
        player.showDialog(build());
    }

    private void onConfirm(Player player, DialogResponseView view) {
        Scheduling.entity(player, () -> {
            PurchaseResult result = marketItem.purchase(market, player);
            Component component = Component.text("1x ").append(marketItem.displayName());
            Component price = marketItem.currency().readablePrice();

            Viewers.sendMessage(player, result.translate(component, price));

            if (result.isSuccess() && ShopsConfig.instance().closeAfterPurchase()) {
                player.closeInventory();
            }
        });
    }

}