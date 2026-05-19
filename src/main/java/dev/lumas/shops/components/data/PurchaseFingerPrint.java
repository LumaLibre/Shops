package dev.lumas.shops.components.data;

import net.kyori.adventure.key.Key;

public record PurchaseFingerPrint(Key marketKey, Key marketItemKey) {

    public boolean isSame(PurchaseFingerPrint other) {
        return marketKey.equals(other.marketKey) && marketItemKey.equals(other.marketItemKey);
    }

    public static PurchaseFingerPrint of(Key marketKey, Key marketItemKey) {
        return new PurchaseFingerPrint(marketKey, marketItemKey);
    }
}
