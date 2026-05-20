package dev.lumas.shops;

import com.google.gson.Gson;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.gson.GsonHolder;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@NullMarked
public class MarketManager {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    private final Path directory = Shops.instance().getDataPath().resolve("markets");
    private final Map<Key, Market> markets = new HashMap<>();

    /**
     * Loads every {@code .json} file in the markets directory into memory.
     * Existing in-memory markets are cleared first.
     */
    public void loadAll() {
        markets.clear();

        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            LOGGER.error("Failed to create markets directory at " + directory, e);
            return;
        }

        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> p.toString().endsWith(".json"))
                    .forEach(this::loadFile);
        } catch (IOException e) {
            LOGGER.error("Failed to list markets directory", e);
        }
    }

    /**
     * Reloads a single market from disk, replacing the in-memory instance.
     * Any players currently viewing the old instance have their inventory closed.
     *
     * @return the new market, or {@code null} if the file does not exist or failed to load
     */
    public @Nullable Market reload(Key key) {
        Market old = markets.get(key);
        if (old != null) {
            closeViewers(old);
            markets.remove(key);
        }

        Path file = fileFor(key);
        if (!Files.exists(file)) {
            return null;
        }
        return loadFile(file);
    }

    /**
     * Saves every in-memory market to its own file.
     * Call this from a scheduled task for autosave, or on shutdown.
     */
    public void saveAll() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            LOGGER.error("Failed to create markets directory at " + directory, e);
            return;
        }

        for (Market market : markets.values()) {
            save(market);
        }
    }

    /**
     * Saves a single market to disk.
     */
    public void save(Market market) {
        Path file = fileFor(market.key());
        Gson gson = GsonHolder.instance().get();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(market, Market.class, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save market " + market.key(), e);
        }
    }

    public @Nullable Market get(Key key) {
        return markets.get(key);
    }

    public Collection<Market> all() {
        return Collections.unmodifiableCollection(markets.values());
    }

    public void add(Market market) {
        markets.put(market.key(), market);
    }

    public @Nullable Market remove(Key key) {
        Market removed = markets.remove(key);
        if (removed != null) {
            closeViewers(removed);
        }
        return removed;
    }

    private @Nullable Market loadFile(Path file) {
        Gson gson = GsonHolder.instance().get();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            Market market = gson.fromJson(reader, Market.class);
            if (market == null) {
                LOGGER.warning("Market file " + file.getFileName() + " deserialized to null, skipping");
                return null;
            }
            markets.put(market.key(), market);
            return market;
        } catch (IOException e) {
            LOGGER.error("Failed to load market file " + file.getFileName(), e);
            return null;
        }
    }

    private Path fileFor(Key key) {
        // Slashes in namespaced keys would break the filename; flatten them.
        String safe = key.asString().replace(':', '_').replace('/', '_');
        return directory.resolve(safe + ".json");
    }

    private void closeViewers(Market market) {
        // Copy first; closeInventory mutates the viewer list.
        List<HumanEntity> viewers = new ArrayList<>(market.getInventory().getViewers());
        for (HumanEntity viewer : viewers) {
            if (viewer instanceof Player player) {
                player.closeInventory();
            }
        }
    }
}