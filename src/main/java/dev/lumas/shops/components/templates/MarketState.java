package dev.lumas.shops.components.templates;

import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.components.data.PurchaseReceipt;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Mutable shared state for a market, persisted to {@code markets/<key>.state.json}.
 * One instance per market key, shared across all players viewing it.
 */
@NullMarked
@Accessors(fluent = true)
public record MarketState(Key key, Map<PurchaseReceipt, Integer> receipts) implements Keyed {

    public MarketState(Key key) {
        this(key, new LinkedHashMap<>());
    }

    public int getPurchasesOf(PurchaseReceipt receipt) {
        return receipts.getOrDefault(receipt, 0);
    }

    public int getGlobalPurchasesOf(Key marketItemKey) {
        int total = 0;
        for (Map.Entry<PurchaseReceipt, Integer> entry : receipts.entrySet()) {
            if (entry.getKey().marketItemKey().equals(marketItemKey)) {
                total += entry.getValue();
            }
        }
        return total;
    }

    public int getRemainingGlobalStock(int globalStock, Key marketItemKey) {
        return globalStock - getGlobalPurchasesOf(marketItemKey);
    }

    public void addPurchase(PurchaseReceipt receipt) {
        receipts.merge(receipt, 1, Integer::sum);
        MarketManager.INSTANCE.save(this);
    }
}