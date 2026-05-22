package dev.lumas.shops.interfaces;

import dev.lumas.shops.components.data.KeyConsumerRegistry;
import io.papermc.paper.dialog.Dialog;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.entity.Player;

import java.util.Locale;

@AllArgsConstructor
public abstract class ShopsDialog {

    private final Locale locale;

    protected final Component translate(String key, ComponentLike... args) {
        return GlobalTranslator.render(Component.translatable(key, args), locale);
    }

    protected final Component translate(String key, Object... args) {
        ComponentLike[] argsAsComponents = new ComponentLike[args.length];
        for (int i = 0; i < args.length; i++) {
            argsAsComponents[i] = Component.text(args[i].toString());
        }
        return GlobalTranslator.render(Component.translatable(key, argsAsComponents), locale);
    }

    public abstract Dialog build();

    public abstract void show(Player player);
}
