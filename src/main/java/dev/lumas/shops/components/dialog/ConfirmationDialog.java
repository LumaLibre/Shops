package dev.lumas.shops.components.dialog;

import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.interfaces.ShopsDialog;
import dev.lumas.shops.util.Scheduling;
import io.papermc.paper.dialog.Dialog;
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

import java.util.List;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class ConfirmationDialog extends ShopsDialog {

    private final KeyConsumer<ConfirmationDialog> yes = KeyConsumer.of(
            this,
            Key.key("shops:confirm/yes"),
            (parent, player, _) -> {
                Scheduling.entity(player, () -> {
                    var result = parent.marketItem.purchase(parent.market, player);
                    player.sendMessage("Result: " + result);
                });
            }
    );
    private final KeyConsumer<ConfirmationDialog> no = KeyConsumer.of(
            this,
            Key.key("shops:confirm/no"),
            (parent, player, _) -> {
                parent.market.open(player);
            }
    );

    private final Market market;
    private final MarketItem marketItem;

    public ConfirmationDialog(Player player, Market market, MarketItem marketItem) {
        super(player.locale());
        this.market = market;
        this.marketItem = marketItem;
    }

    @Override
    public Dialog build() {
        ItemStack itemStack = marketItem.stack();
        Component price = marketItem.currency().readablePrice();

        DialogBody body = DialogBody.item(itemStack)
                .description(DialogBody.plainMessage(translate("shops.confirm.content", price)))
                .build();

        ActionButton yesButton = ActionButton.builder(translate("shops.confirm.button.yes"))
                .action(DialogAction.customClick(yes.key(), null))
                .build();

        ActionButton noButton = ActionButton.builder(translate("shops.confirm.button.no"))
                .action(DialogAction.customClick(no.key(), null))
                .build();

        DialogBase base = DialogBase.builder(translate("shops.confirm.title"))
                .canCloseWithEscape(true)
                .body(List.of(body))
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
}