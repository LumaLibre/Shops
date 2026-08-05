package dev.lumas.shops.components.dialog.session;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.listeners.ItemStackPickListener;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class CurrencyFlow {

    public static void start(Currencies type, AddItemSession session, Player player, Runnable onComplete, Runnable onCancel) {
        switch (type) {
            case MONEY -> new MoneyCurrencyDialog(player.locale(), session, onComplete).show(player);
            case LUMAITEMS -> new LumaItemCurrencyDialog(player.locale(), session, onComplete).show(player);
            case ITEMSTACK -> ItemStackPickListener.INSTANCE.begin(session, player, onComplete, onCancel);
        }
    }
}
