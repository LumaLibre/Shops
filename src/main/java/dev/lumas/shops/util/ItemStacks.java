package dev.lumas.shops.util;

import dev.lumas.shops.config.TranslatorService;
import io.papermc.paper.datacomponent.DataComponentTypes;
import lombok.experimental.UtilityClass;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@UtilityClass
public final class ItemStacks {

    @SuppressWarnings("UnstableApiUsage")
    public static ItemStack displaySafe(ItemStack stack) {
        ItemStack copy = stack.clone();
        if (copy.hasData(DataComponentTypes.MAX_DAMAGE)
                && copy.getDataOrDefault(DataComponentTypes.MAX_STACK_SIZE, 1) > 1) {
            copy.unsetData(DataComponentTypes.MAX_DAMAGE);
            copy.unsetData(DataComponentTypes.DAMAGE);
        }
        return copy;
    }

    public static void addLines(List<Component> target, Locale locale, String key, Object... args) {
        String raw = TranslatorService.instance().getMiniMessageString(key, locale);
        if (raw == null) {
            target.add(translate(locale, key, args));
            return;
        }

        MiniMessage mm = MiniMessage.miniMessage();

        for (String segment : raw.split("\\\\n|\n", -1)) {
            if (segment.isEmpty()) {
                target.add(Component.empty());
                continue;
            }

            Component component = mm.deserialize(segment);
            for (int i = 0; i < args.length; i++) {
                component = substituteArg(component, i, args[i]);
            }
            target.add(component.decoration(TextDecoration.ITALIC, false));
        }
    }

    private static Component substituteArg(Component source, int index, Object arg) {
        String token = "<arg:" + index + ">";
        Pattern pattern = Pattern.compile(Pattern.quote(token));

        if (arg instanceof ComponentLike c) {
            Component replacement = c.asComponent();
            return source.replaceText(builder -> builder.match(pattern).replacement(replacement));
        }
        String replacement = String.valueOf(arg);
        return source.replaceText(builder -> builder.match(pattern).replacement(replacement));
    }

    public static Component translate(Locale locale, String key, Object... args) {
        ComponentLike[] argsAsComponents = Viewers.asComponents(args);
        return GlobalTranslator.render(Component.translatable(key, argsAsComponents), locale).decoration(TextDecoration.ITALIC, false);
    }
}
