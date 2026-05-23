package dev.lumas.shops.interfaces;

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

    public abstract Dialog build();

    public abstract void show(Player player);
}
