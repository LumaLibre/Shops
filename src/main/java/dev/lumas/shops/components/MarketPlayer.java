package dev.lumas.shops.components;

import com.google.common.base.Preconditions;
import dev.lumas.shops.components.data.PurchaseFingerPrint;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@NullMarked
@RequiredArgsConstructor
public class MarketPlayer {

    private final UUID uuid;
    private final Map<PurchaseFingerPrint, Integer> purchased = new HashMap<>();


    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public Player getPlayerOrThrow() {
        return Preconditions.checkNotNull(getPlayer(), "Player not online");
    }

    public int getPurchasesOf(PurchaseFingerPrint fingerPrint) {
        return purchased.getOrDefault(fingerPrint, 0);
    }

    public void addPurchase(PurchaseFingerPrint fingerPrint) {
        purchased.put(fingerPrint, purchased.getOrDefault(fingerPrint, 0) + 1);
    }
}
