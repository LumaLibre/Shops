package dev.lumas.shops.components.dialog;

import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.util.Scheduling;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Locale;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class ConfirmationDialog {

    private static final KeyConsumer<ConfirmationDialog> YES = KeyConsumer.of(
            Key.key("shops:confirm/yes"),
            ConfirmationDialog::onConfirm
    );
    private static final KeyConsumer<ConfirmationDialog> NO = KeyConsumer.of(
            Key.key("shops:confirm/no"),
            ConfirmationDialog::onCancel
    );

    private final Market market;
    private final MarketItem marketItem;

    public ConfirmationDialog(Market market, MarketItem marketItem) {
        this.market = market;
        this.marketItem = marketItem;
    }

    public void show(Player player) {
        KeyConsumerRegistry.INSTANCE.register(player, YES.withParent(this), NO.withParent(this));
        player.showDialog(buildFor(player));
    }

    private Dialog buildFor(Player player) {
        Locale locale = player.locale();
        ItemStack itemStack = marketItem.stack();
        String price = marketItem.currency().readablePrice();

        Component title = tr(locale, "shops.confirm.title");
        Component description = tr(locale, "shops.confirm.content", Component.text(price));
        Component yesLabel = tr(locale, "shops.confirm.button.yes");
        Component noLabel = tr(locale, "shops.confirm.button.no");

        DialogBody body = DialogBody.item(itemStack)
                .description(DialogBody.plainMessage(description))
                .build();

        ActionButton yes = ActionButton.builder(yesLabel)
                .action(DialogAction.customClick(YES.key(), null))
                .build();

        ActionButton no = ActionButton.builder(noLabel)
                .action(DialogAction.customClick(NO.key(), null))
                .build();

        DialogBase base = DialogBase.builder(title)
                .canCloseWithEscape(true)
                .body(List.of(body))
                .build();

        return Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(yes, no))
        );
    }

    private static Component tr(Locale locale, String key, ComponentLike... args) {
        return GlobalTranslator.render(Component.translatable(key, args), locale);
    }

    private void onConfirm(Player player) {
        Scheduling.entity(player, () -> {
            var result = marketItem.purchase(market, player);
            player.sendMessage("Result: " + result);
        });
    }

    private void onCancel(Player player) {
        market.open(player);
    }
}