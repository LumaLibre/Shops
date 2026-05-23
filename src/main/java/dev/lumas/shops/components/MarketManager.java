package dev.lumas.shops.components;

import com.google.gson.Gson;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.data.SlotEntry;
import dev.lumas.shops.components.data.SlotList;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.gson.GsonHolder;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Owns market templates and shared state. Builds a fresh {@link Market} for each player.
 *
 * <p><b>Templates</b> ({@code markets/<key>.json}) are immutable definitions.
 * Cached; invalidate via {@link #invalidate(Key)} / {@link #invalidateAll()}.
 *
 * <p><b>State</b> ({@code markets/<key>.state.json}) holds mutable stock and sales.
 * Shared across viewers of the same market. Save with {@link #save(MarketState)}.
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

    /** Single-thread executor so state saves run off-main but never race each other. */
    private final ExecutorService saveExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "shops-state-saver");
        t.setDaemon(true);
        return t;
    });


    public @Nullable Market market(Key key) {
        MarketTemplate template = template(key);
        if (template == null) return null;
        return template.toMarket(state(key));
    }

    public boolean exists(Key key) {
        return Files.exists(templateFileFor(key));
    }

    public void invalidate(Key key) {
        templateCache.remove(key);
    }

    public void invalidateAll() {
        templateCache.clear();
    }

    public void save(MarketState state) {
        Key key = state.key();
        saveExecutor.execute(() -> writeState(key, state));
    }

    public void shutdown() {
        saveExecutor.shutdown();
    }


    @SneakyThrows
    public MarketTemplate create(Key key, Component title, int size, List<SlotEntry> staticSlots, SlotList contentSlots) {
        if (exists(key)) {
            throw new IllegalStateException("Market already exists: " + key);
        }
        return writeAndCache(new MarketTemplate(key, title, size, contentSlots, staticSlots, Map.of()));
    }

    /**
     * Edits an existing market template. Null arguments keep the current value.
     *
     * @throws IllegalStateException if no template exists for {@code key}
     */
    @SneakyThrows
    public MarketTemplate edit(Key key,
                               @Nullable Component title,
                               @Nullable Integer size,
                               @Nullable List<SlotEntry> staticSlots,
                               @Nullable SlotList contentSlots) {
        MarketTemplate current = requireTemplate(key);
        return writeAndCache(new MarketTemplate(
                current.key(),
                title != null ? title : current.title(),
                size != null ? size : current.size(),
                contentSlots != null ? contentSlots : current.contentSlots(),
                staticSlots != null ? staticSlots : current.staticSlots(),
                current.items()
        ));
    }

    /** Appends {@code item} to the end of the market's item list. */
    public MarketTemplate addItem(Key marketKey, MarketItem item) {
        return addItem(marketKey, item, Integer.MAX_VALUE);
    }

    /**
     * Inserts {@code item} at the given position. {@code index} is clamped to {@code [0, size]}.
     *
     * @throws IllegalStateException if the market is missing or the item key is already present
     */
    @SneakyThrows
    public MarketTemplate addItem(Key marketKey, MarketItem item, int index) {
        MarketTemplate current = requireTemplate(marketKey);
        if (current.items().containsKey(item.key())) {
            throw new IllegalStateException("Item already exists in market: " + item.key());
        }

        Map<Key, MarketItem> updated = new LinkedHashMap<>(current.items().size() + 1);
        int clamped = Math.max(0, Math.min(index, current.items().size()));
        int i = 0;
        boolean inserted = false;
        for (Map.Entry<Key, MarketItem> entry : current.items().entrySet()) {
            if (i == clamped && !inserted) {
                updated.put(item.key(), item);
                inserted = true;
            }
            updated.put(entry.getKey(), entry.getValue());
            i++;
        }
        if (!inserted) updated.put(item.key(), item);

        return writeAndCache(withItems(current, updated));
    }

    /**
     * Removes the item with {@code itemKey} from the market.
     *
     * @throws IllegalStateException if the market is missing or the item is not present
     */
    @SneakyThrows
    public MarketTemplate removeItem(Key marketKey, Key itemKey) {
        MarketTemplate current = requireTemplate(marketKey);
        if (!current.items().containsKey(itemKey)) {
            throw new IllegalStateException("Item does not exist in market: " + itemKey);
        }

        Map<Key, MarketItem> updated = new LinkedHashMap<>(current.items());
        updated.remove(itemKey);

        return writeAndCache(withItems(current, updated));
    }


    public Set<Key> keys() {
        Set<Key> keys = new HashSet<>();
        if (!Files.isDirectory(directory)) return keys;

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

    public @Nullable MarketTemplate template(Key key) {
        MarketTemplate cached = templateCache.get(key);
        if (cached != null) return cached;

        Path file = templateFileFor(key);
        if (!Files.exists(file)) return null;

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


    private MarketTemplate requireTemplate(Key key) {
        MarketTemplate current = template(key);
        if (current == null) {
            throw new IllegalStateException("No market exists for key: " + key);
        }
        return current;
    }

    private static MarketTemplate withItems(MarketTemplate base, Map<Key, MarketItem> items) {
        return new MarketTemplate(base.key(), base.title(), base.size(), base.contentSlots(), base.staticSlots(), items);
    }

    @SneakyThrows
    private MarketTemplate writeAndCache(MarketTemplate template) {
        Files.createDirectories(directory);
        Path file = templateFileFor(template.key());
        Gson gson = GsonHolder.instance().get();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(template, MarketTemplate.class, writer);
        }
        templateCache.put(template.key(), template);
        return template;
    }

    private MarketState state(Key key) {
        MarketState cached = stateCache.get(key);
        if (cached != null) return cached;

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
        return key.asString().replace(':', '_').replace('/', '_');
    }

    @SuppressWarnings("PatternValidation")
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