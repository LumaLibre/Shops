package dev.lumas.shops.components;

import com.google.gson.Gson;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.gson.GsonHolder;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Owns market templates and shared state. Builds a fresh {@link Market} for each player.
 *
 * <p><b>Templates</b> ({@code markets/<key>.json}) are immutable definitions edited
 * externally. Cached in memory; the cache is cleared by {@link #invalidate(Key)}
 * or {@link #invalidateAll()}, which an admin command should call after editing.
 *
 * <p><b>State</b> ({@code markets/<key>.state.json}) holds mutable bits like stock
 * counts and sales totals. Shared across players viewing the same market.
 * Save with {@link #save(MarketState)} after mutating.
 */
@NullMarked
public class MarketManager {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();
    private static final String STATE_SUFFIX = ".state.json";
    private static final String TEMPLATE_SUFFIX = ".json";

    public static final MarketManager INSTANCE = new MarketManager();

    private final Path directory = Shops.instance().getDataPath().resolve("markets");
    private final Map<Key, MarketTemplate> templateCache = new HashMap<>();
    private final Map<Key, MarketState> stateCache = new HashMap<>();

    /**
     * Single-thread executor so state saves run off the main thread but never race
     * each other. The plugin should call {@link #shutdown()} on disable to drain it.
     */
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "shops-state-saver");
        t.setDaemon(true);
        return t;
    });

    /**
     * Builds a fresh {@link Market} for the given player. Each call returns a new
     * instance so per-player UI state is isolated; the underlying {@link MarketState}
     * is shared across all callers for the same key.
     *
     * @return the constructed market, or {@code null} if no template exists for {@code key}
     */
    public @Nullable Market market(Key key) {
        MarketTemplate template = template(key);
        if (template == null) {
            return null;
        }
        MarketState state = state(key);
        return template.toMarket(state);
    }

    /**
     * @return true if a template file exists on disk for {@code key}
     *         (not necessarily loaded into the cache)
     */
    public boolean exists(Key key) {
        return Files.exists(templateFileFor(key));
    }


    /**
     * Drops the cached template for {@code key}. The next {@link #market(Key)} call
     * will re-read the file. State is left alone.
     *
     * <p>Call this from your reload command after an admin edits a market file.
     */
    public void invalidate(Key key) {
        templateCache.remove(key);
    }

    /**
     * Drops every cached template. Use for a global reload.
     */
    public void invalidateAll() {
        templateCache.clear();
    }


    /**
     * Asynchronously writes the given state to disk. Saves are serialised so
     * calls cannot race each other.
     */
    public void save(MarketState state) {
        // Snapshot any data the executor needs *now*, on the calling thread.
        // If MarketState had mutable collections you'd need a deep copy here.
        Key key = state.key();
        saveExecutor.execute(() -> writeState(key, state));
    }

    /**
     * Flushes pending saves and stops the executor. Call from plugin disable.
     */
    public void shutdown() {
        saveExecutor.shutdown();
    }

    /**
     * Eagerly loads every template + state file. Optionalally, {@link #market(Key)} loads
     * lazily on first access — but useful for catching JSON errors at startup.
     */
    public void loadAll() {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            LOGGER.error("Failed to create markets directory at " + directory, e);
            return;
        }

        try (Stream<Path> files = Files.list(directory)) {
            Set<Key> seen = new HashSet<>();
            files.filter(p -> {
                String name = p.getFileName().toString();
                // Templates only — state files load on demand alongside their template.
                return name.endsWith(TEMPLATE_SUFFIX) && !name.endsWith(STATE_SUFFIX);
            }).forEach(file -> {
                Key key = keyFromTemplateFile(file);
                if (key != null && seen.add(key)) {
                    template(key);  // populates the cache
                }
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list markets directory", e);
        }
    }

    public Set<Key> keys() {
        Set<Key> keys = new HashSet<>();
        if (!Files.isDirectory(directory)) {
            return keys;
        }
        try (Stream<Path> files = Files.list(directory)) {
            files.filter(p -> {
                String name = p.getFileName().toString();
                return name.endsWith(TEMPLATE_SUFFIX) && !name.endsWith(STATE_SUFFIX);
            }).forEach(file -> {
                Key key = keyFromTemplateFile(file);
                if (key != null) keys.add(key);
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list markets directory", e);
        }
        return keys;
    }


    private @Nullable MarketTemplate template(Key key) {
        MarketTemplate cached = templateCache.get(key);
        if (cached != null) {
            return cached;
        }

        Path file = templateFileFor(key);
        if (!Files.exists(file)) {
            return null;
        }

        Gson gson = GsonHolder.instance().get();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            MarketTemplate loaded = gson.fromJson(reader, MarketTemplate.class);
            if (loaded == null) {
                LOGGER.warning("Market template " + file.getFileName() + " deserialized to null");
                return null;
            }
            templateCache.put(key, loaded);
            return loaded;
        } catch (IOException e) {
            LOGGER.error("Failed to load market template " + file.getFileName(), e);
            return null;
        }
    }

    private MarketState state(Key key) {
        MarketState cached = stateCache.get(key);
        if (cached != null) {
            return cached;
        }

        Path file = stateFileFor(key);
        MarketState loaded;
        if (Files.exists(file)) {
            Gson gson = GsonHolder.instance().get();
            try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
                MarketState fromDisk = gson.fromJson(reader, MarketState.class);
                loaded = fromDisk != null ? fromDisk : new MarketState(key);
            } catch (IOException e) {
                LOGGER.error("Failed to load market state " + file.getFileName() + ", starting fresh", e);
                loaded = new MarketState(key);
            }
        } else {
            loaded = new MarketState(key);
        }

        stateCache.put(key, loaded);
        return loaded;
    }

    private void writeState(Key key, MarketState state) {
        try {
            Files.createDirectories(directory);
        } catch (IOException e) {
            LOGGER.error("Failed to create markets directory at " + directory, e);
            return;
        }

        Path file = stateFileFor(key);
        Gson gson = GsonHolder.instance().get();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(state, MarketState.class, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save market state " + key, e);
        }
    }

    private Path templateFileFor(Key key) {
        return directory.resolve(safeName(key) + TEMPLATE_SUFFIX);
    }

    private Path stateFileFor(Key key) {
        return directory.resolve(safeName(key) + STATE_SUFFIX);
    }

    private String safeName(Key key) {
        // Slashes in namespaced keys would break the filename; flatten them.
        return key.asString().replace(':', '_').replace('/', '_');
    }

    private @Nullable Key keyFromTemplateFile(Path file) {
        String name = file.getFileName().toString();
        if (!name.endsWith(TEMPLATE_SUFFIX)) return null;
        String stem = name.substring(0, name.length() - TEMPLATE_SUFFIX.length());

        int firstUnderscore = stem.indexOf('_');
        if (firstUnderscore < 0) return null;
        String namespace = stem.substring(0, firstUnderscore);
        String value = stem.substring(firstUnderscore + 1).replace('_', '/');
        try {
            return Key.key(namespace, value);
        } catch (Exception e) {
            LOGGER.warning("Could not parse market key from filename: " + name);
            return null;
        }
    }
}