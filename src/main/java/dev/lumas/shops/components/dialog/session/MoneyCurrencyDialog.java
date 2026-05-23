package dev.lumas.shops.components.dialog.session;

import dev.lumas.shops.components.currency.MoneyCurrencyImpl;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.interfaces.ShopsDialog;
import dev.lumas.shops.util.Numbers;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Locale;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class MoneyCurrencyDialog extends ShopsDialog {

    private static final String INPUT_AMOUNT = "amount";

    private final KeyConsumer<MoneyCurrencyDialog> submit = KeyConsumer.of(
            this,
            Key.key("shops:additem/money/submit"),
            MoneyCurrencyDialog::onSubmit
    );

    private final KeyConsumer<MoneyCurrencyDialog> cancel = KeyConsumer.of(
            this,
            Key.key("shops:additem/money/cancel"),
            (_, player, _) -> {
                Viewers.sendMessage(player, "shops.messages.cancelled");
            }
    );

    private final AddItemSession session;
    private final Runnable onComplete;

    public MoneyCurrencyDialog(Locale locale, AddItemSession session, Runnable onComplete) {
        super(locale);
        this.session = session;
        this.onComplete = onComplete;
    }

    @Override
    public Dialog build() {
        DialogInput amountInput = DialogInput.text(INPUT_AMOUNT, translate("shops.additem.money.amount"))
                .maxLength(16)
                .initial("100")
                .width(200)
                .build();

        ActionButton submitButton = ActionButton.builder(translate("shops.additem.button.submit"))
                .action(DialogAction.customClick(submit.key(), null))
                .build();

        ActionButton cancelButton = ActionButton.builder(translate("shops.additem.button.cancel"))
                .action(DialogAction.customClick(cancel.key(), null))
                .build();

        DialogBase base = DialogBase.builder(translate("shops.additem.money.title"))
                .canCloseWithEscape(false)
                .inputs(List.of(amountInput))
                .build();

        return Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(submitButton, cancelButton))
        );
    }

    private void onSubmit(Player player, DialogResponseView view) {
        String raw = view.getText(INPUT_AMOUNT);
        double amount = Numbers.parseDouble(raw, 0);
        session.currency(new MoneyCurrencyImpl(amount));
        onComplete.run();
    }

    @Override
    public void show(Player player) {
        KeyConsumerRegistry.INSTANCE.register(player, submit);
        player.showDialog(build());
    }
}