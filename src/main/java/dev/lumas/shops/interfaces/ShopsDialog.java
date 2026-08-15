package dev.lumas.shops.interfaces;

import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.util.ItemStacks;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.body.DialogBody;
import io.papermc.paper.registry.data.dialog.body.PlainMessageDialogBody;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Locale;

/**
 * Represents a dialog used by this plugin.
 */
@AllArgsConstructor
public abstract class ShopsDialog {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

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

    @SuppressWarnings("UnstableApiUsage")
    protected final DialogBody itemBody(ItemStack stack, Component description) {
        PlainMessageDialogBody message = DialogBody.plainMessage(description);
        try {
            return DialogBody.item(ItemStacks.displaySafe(stack)).description(message).build();
        } catch (RuntimeException e) {
            LOGGER.warning("Cannot preview " + stack.getType() + " in a dialog, falling back to text only: " + e.getMessage());
            return message;
        }
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
