package dev.lumas.shops.util;

import lombok.experimental.UtilityClass;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

@UtilityClass
public final class Viewers {

    private static final Component PREFIX = Component.translatable("shops.messages.prefix");

    public static void sendMessage(Audience audience, Component message) {
        audience.sendMessage(PREFIX.equals(Component.empty()) ? message : PREFIX.append(Component.space()).append(message));
    }

    public static void sendMessage(Audience audience, String key, Object... args) {
        ComponentLike[] components = asComponents(args);
        Component message = Component.translatable(key, components);

        Component finalMessage = PREFIX.equals(Component.empty()) ? message : PREFIX.append(Component.space()).append(message);
        audience.sendMessage(finalMessage);
    }

    public static ComponentLike[] asComponents(Object... args) {
        ComponentLike[] components = new ComponentLike[args.length];
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            components[i] = arg instanceof ComponentLike ? (ComponentLike) arg : Component.text(arg.toString());
        }
        return components;
    }
}
