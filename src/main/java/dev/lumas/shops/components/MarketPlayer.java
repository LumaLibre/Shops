package dev.lumas.shops.components;

import com.google.common.base.Preconditions;
import dev.lumas.shops.components.serial.SerialKeyToIntMap;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

@NullMarked
@RequiredArgsConstructor
public class MarketPlayer {

    private final UUID uuid;
    private final SerialKeyToIntMap<Key> purchased = SerialKeyToIntMap.ofRaw();


    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public Player getPlayerOrThrow() {
        return Preconditions.checkNotNull(getPlayer(), "Player not online");
    }

    public int getPurchasesOf(Key shopItemKey) {
        return purchased.get().getOrDefault(shopItemKey, 0);
    }

    public void addPurchase(Key shopItemKey) {
        Map<Key, Integer> map = purchased.get();
        map.put(shopItemKey, map.getOrDefault(shopItemKey, 0) + 1);
    }
}
