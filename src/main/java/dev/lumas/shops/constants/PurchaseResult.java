package dev.lumas.shops.constants;

import dev.lumas.shops.util.Viewers;
import lombok.AllArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

@AllArgsConstructor
public enum PurchaseResult {
    SUCCESS("shops.messages.purchased"),
    NOT_ENOUGH_CURRENCY("shops.messages.not_enough_currency"),
    NOT_ENOUGH_STOCK("shops.messages.not_enough_stock"),
    TOO_MANY_PURCHASES("shops.messages.too_many_purchases");

    private final String translationKey;

    public Component translate(Object... args) {
        ComponentLike[] comps = Viewers.asComponents(args);
        return Component.translatable(translationKey, comps);
    }

    public boolean isSuccess() {
        return this == SUCCESS;
    }
}