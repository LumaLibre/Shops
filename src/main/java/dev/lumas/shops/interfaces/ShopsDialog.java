package dev.lumas.shops.interfaces;

import io.papermc.paper.dialog.Dialog;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * Represents a dialog used by this plugin.
 */
@AllArgsConstructor
public abstract class ShopsDialog {

    private final Locale locale;

    protected final Component translate(String key, ComponentLike... args) {
        return GlobalTranslator.render(Component.translatable(key, args), locale);
    }

    /**
     * The locale this dialog renders in, for content that isn't a plain translation key.
     * @return The viewer's locale.
     */
    protected final Locale locale() {
        return locale;
    }

    /**
     * Builds the dialog.
     * @return The built dialog.
     */
    public abstract Dialog build();

    /**
     * Shows the dialog to the player.
     * @param player The player to show the dialog to.
     */
    public abstract void show(Player player);
}
