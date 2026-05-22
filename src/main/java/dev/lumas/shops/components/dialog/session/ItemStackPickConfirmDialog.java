package dev.lumas.shops.components.dialog.session;

import dev.lumas.shops.components.currency.ItemStackCurrencyImpl;
import dev.lumas.shops.components.data.KeyConsumer;
import dev.lumas.shops.components.data.KeyConsumerRegistry;
import dev.lumas.shops.interfaces.ShopsDialog;
import dev.lumas.shops.listeners.ItemStackPickListener;
import dev.lumas.shops.util.Numbers;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.dialog.DialogResponseView;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import io.papermc.paper.registry.data.dialog.type.DialogType;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Locale;

@NullMarked
@SuppressWarnings("UnstableApiUsage")
public class ItemStackPickConfirmDialog extends ShopsDialog {

    private static final String INPUT_AMOUNT = "amount";

    private final KeyConsumer<ItemStackPickConfirmDialog> confirm = KeyConsumer.of(
            this,
            Key.key("shops:additem/itemstack/confirm"),
            ItemStackPickConfirmDialog::onConfirm
    );
    private final KeyConsumer<ItemStackPickConfirmDialog> reject = KeyConsumer.of(
            this,
            Key.key("shops:additem/itemstack/reject"),
            ItemStackPickConfirmDialog::onReject
    );

    private final AddItemSession session;
    private final ItemStack picked;
    private final Runnable onComplete;
    private final Runnable onCancel;

    public ItemStackPickConfirmDialog(Locale locale, AddItemSession session, ItemStack picked, Runnable onComplete, Runnable onCancel) {
        super(locale);
        this.session = session;
        this.picked = picked;
        this.onComplete = onComplete;
        this.onCancel = onCancel;
    }

    @Override
    public Dialog build() {
        DialogInput amountInput = DialogInput.text(INPUT_AMOUNT, translate("shops.additem.itemstack.amount"))
                .maxLength(8)
                .initial(String.valueOf(picked.getAmount()))
                .width(150)
                .build();

        ActionButton confirmButton = ActionButton.builder(translate("shops.additem.itemstack.confirm"))
                .action(DialogAction.customClick(confirm.key(), null))
                .build();

        ActionButton rejectButton = ActionButton.builder(translate("shops.additem.itemstack.repick"))
                .action(DialogAction.customClick(reject.key(), null))
                .build();

        DialogBase base = DialogBase.builder(translate("shops.additem.itemstack.confirm_title"))
                .canCloseWithEscape(false)
                .body(List.of(
                        DialogBody.item(picked.asOne())
                                .description(DialogBody.plainMessage(translate("shops.additem.itemstack.confirm_body")))
                                .build()
                ))
                .inputs(List.of(amountInput))
                .build();

        return Dialog.create(b -> b.empty()
                .base(base)
                .type(DialogType.confirmation(confirmButton, rejectButton))
        );
    }

    private void onConfirm(Player player, DialogResponseView view) {
        int amount = Numbers.parseInt(view.getText(INPUT_AMOUNT), picked.getAmount());
        if (amount < 1) amount = 1;
        session.currency(ItemStackCurrencyImpl.of(picked, amount));
        onComplete.run();
    }

    private void onReject(Player player, DialogResponseView view) {
        // Restart the pick. If they time out the next round, they go back to the main dialog.
        ItemStackPickListener.INSTANCE.begin(session, player, onComplete, onCancel);
    }

    @Override
    public void show(Player player) {
        KeyConsumerRegistry.INSTANCE.register(player, confirm, reject);
        player.showDialog(build());
    }
}