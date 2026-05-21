package dev.lumas.shops.components.templates;

import dev.lumas.shops.components.data.PurchaseReceipt;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

/**
 * Mutable shared state for a market, persisted to {@code markets/<key>.state.json}.
 * One instance per market key, shared across all players viewing it.
 *
 * <p>Mutations should go through {@link dev.lumas.shops.MarketManager#save(MarketState)}
 * to write the change to disk.
 */
@Getter
@NullMarked
@Accessors(fluent = true)
public class MarketState implements Keyed {

    private final Key key;
    private final Map<PurchaseReceipt, Integer> receipts;

    public MarketState(Key key) {
        this(key, new HashMap<>());
    }

    public MarketState(Key key, Map<PurchaseReceipt, Integer> receipts) {
        this.key = key;
        this.receipts = receipts;
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

    public void addPurchase(PurchaseReceipt receipt) {
        receipts.merge(receipt, 1, Integer::sum);
    }
}