package dev.lumas.shops.components.dialog.session;

import dev.lumas.shops.components.currency.LumaItemsCurrencyImpl;
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
public class LumaItemCurrencyDialog extends ShopsDialog {

    private static final String INPUT_KEY = "key";
    private static final String INPUT_AMOUNT = "amount";

    private final KeyConsumer<LumaItemCurrencyDialog> submit = KeyConsumer.of(
            this,
            Key.key("shops:additem/lumaitem/submit"),
            LumaItemCurrencyDialog::onSubmit
    );

    private final KeyConsumer<LumaItemCurrencyDialog> cancel = KeyConsumer.of(
            this,
            Key.key("shops:additem/lumaitem/cancel"),
            (_, player, _) -> {
                Viewers.sendMessage(player, "shops.messages.cancelled");
            }
    );

    private final AddItemSession session;
    private final Runnable onComplete;

    public LumaItemCurrencyDialog(Locale locale, AddItemSession session, Runnable onComplete) {
        super(locale);
        this.session = session;
        this.onComplete = onComplete;
    }

    @Override
    public Dialog build() {
        DialogInput keyInput = DialogInput.text(INPUT_KEY, translate("shops.additem.lumaitem.key"))
                .maxLength(64).width(250).build();

        DialogInput amountInput = DialogInput.text(INPUT_AMOUNT, translate("shops.additem.lumaitem.amount"))
                .maxLength(16).initial("1").width(150).build();

        ActionButton submitButton = ActionButton.builder(translate("shops.additem.button.submit"))
                .action(DialogAction.customClick(submit.key(), null))
                .build();

        ActionButton cancelButton = ActionButton.builder(translate("shops.additem.button.cancel"))
                .action(DialogAction.customClick(cancel.key(), null))
                .build();


        DialogBase base = DialogBase.builder(translate("shops.additem.lumaitem.title"))
                .canCloseWithEscape(false)
                .inputs(List.of(keyInput, amountInput))
                .build();

        return Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(submitButton, cancelButton))
        );
    }

    private void onSubmit(Player player, DialogResponseView view) {
        String key = view.getText(INPUT_KEY);
        if (key == null || key.isBlank()) {
            Viewers.sendMessage(player, "shops.additem.error.bad_lumaitem_key");
            return;
        }
        int amount = Numbers.parseInt(view.getText(INPUT_AMOUNT), 1);
        session.currency(LumaItemsCurrencyImpl.of(key, amount));
        onComplete.run();
    }

    @Override
    public void show(Player player) {
        KeyConsumerRegistry.INSTANCE.register(player, submit);
        player.showDialog(build());
    }
}