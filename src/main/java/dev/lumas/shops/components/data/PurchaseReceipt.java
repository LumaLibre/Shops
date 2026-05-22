package dev.lumas.shops.components.data;

import net.kyori.adventure.key.Key;

import java.util.UUID;

/**
 * Represents a receipt for a purchase.
 * @param purchaser The UUID of the player who purchased the item.
 * @param marketItemKey The key of the market item that was purchased.
 */
public record PurchaseReceipt(UUID purchaser, Key marketItemKey) {

    public boolean isSame(PurchaseReceipt other) {
        return this.purchaser.equals(other.purchaser) && this.marketItemKey.equals(other.marketItemKey);
    }

    public static PurchaseReceipt of(UUID purchaser, Key marketItemKey) {
        return new PurchaseReceipt(purchaser, marketItemKey);
    }
}
