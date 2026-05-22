package dev.lumas.shops.components.dialog.session;

import dev.lumas.shops.components.dialog.AddMarketItemDialog;
import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.listeners.ItemStackPickListener;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CurrencyFlow {

    public static void start(Currencies type, AddItemSession session, Player player, Runnable onComplete) {
        switch (type) {
            case MONEY -> new MoneyCurrencyDialog(player.locale(), session, onComplete).show(player);
            case LUMAITEMS -> new LumaItemCurrencyDialog(player.locale(), session, onComplete).show(player);
            case ITEMSTACK -> {
                // onCancel: drop the player back at the main dialog so they can pick a different currency.
                Runnable onCancel = () -> new AddMarketItemDialog(player.locale(), session.market(), session.stack()).show(player);
                ItemStackPickListener.INSTANCE.begin(session, player, onComplete, onCancel);
            }
        }
    }
}