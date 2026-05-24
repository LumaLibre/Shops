package dev.lumas.shops.interfaces;

import dev.lumas.core.model.command.AbstractSubCommand;
import dev.lumas.shops.Shops;
import dev.lumas.shops.config.ShopsConfig;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface SubCommand extends AbstractSubCommand<Shops> {

    /**
     * Creates a key with the given string.
     * @param string The string to create the key from.
     * @return The created key.
     */
    @SuppressWarnings("PatternValidation")
    default Key key(String string) {
        if (string.contains(":")){
            return Key.key(string);
        } else {
            String namespace = ShopsConfig.instance().defaultNamespace();
            return Key.key(namespace, string);
        }
    }
}
