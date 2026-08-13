package dev.lumas.shops.api.currency;

import dev.lumas.shops.util.Viewers;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@FunctionalInterface
@NullMarked
public interface CurrencyEditor {

    /**
     * Opens the editor. Implementations must eventually call exactly one of {@code onComplete}
     * or {@code onCancel}, or drop the flow entirely if the admin closes out of it.
     *
     * @param player The admin configuring the item.
     * @param selection The item being configured. Call {@link CurrencySelection#currency(dev.lumas.shops.interfaces.Currency)} before {@code onComplete}, or the item cannot be saved.
     * @param onComplete Run once the currency has been set, to save the item.
     * @param onCancel Run to abandon this currency and return to the add-item dialog.
     */
    void open(Player player, CurrencySelection selection, Runnable onComplete, Runnable onCancel);

    /**
     * An editor for currencies that can only be configured by hand in the market's JSON file.
     * Tells the admin as much and drops them back into the add-item dialog.
     * @return An editor that never sets a currency.
     */
    static CurrencyEditor unsupported() {
        return (player, _, _, onCancel) -> {
            Viewers.sendMessage(player, "shops.additem.error.no_editor");
            onCancel.run();
        };
    }
}
