package dev.lumas.shops.interfaces;

import dev.lumas.core.model.command.AbstractSubCommand;
import dev.lumas.shops.Shops;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface SubCommand extends AbstractSubCommand<Shops> {

    @SuppressWarnings("PatternValidation")
    default Key key(String string) {
        if (string.contains(":")){
            return Key.key(string);
        } else {
            return Key.key("shops:" + string);
        }
    }
}
