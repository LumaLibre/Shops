package dev.lumas.shops.components.dialog;

import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.KeyConsumer;
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

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class ConfirmationDialog {

    private static final KeyConsumer<ConfirmationDialog> YES = KeyConsumer.of(
            Key.key("shops:confirm/yes"),
            dialog -> {
                throw new UnsupportedOperationException();
            }
    );
    private static final KeyConsumer<ConfirmationDialog> NO = KeyConsumer.of(
            Key.key("shops:confirm/no"),
            dialog -> {
                throw new UnsupportedOperationException();
            }
    );

    private final Market market;
    private final Dialog dialog;

    public ConfirmationDialog(Market market, MarketItem marketItem) {
        ItemStack itemStack = marketItem.stack();
        String price = marketItem.currency().price();

        Component title = Component.translatable("shops.confirm.title");
        Component description = Component.translatable("shops.confirm.content", price);

        DialogBody body = DialogBody.item(itemStack)
                .description(DialogBody.plainMessage(description))
                .build();

        ActionButton yes = ActionButton.builder(Component.translatable("shops.confirm.button.yes"))
                .action(DialogAction.customClick(YES, null))
                .build();

        ActionButton no = ActionButton.builder(Component.translatable("shops.confirm.button.no"))
                .action(DialogAction.customClick(NO, null))
                .build();

        DialogBase base = DialogBase.builder(title)
                .canCloseWithEscape(true)
                .body(List.of(body))
                .build();

        this.market = market;
        this.dialog = Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(yes, no))
        );
    }

    public void show(Player player) {
        player.showDialog(dialog);
    }
}
