package dev.lumas.shops.components;

import com.google.gson.Gson;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.data.SlotEntry;
import dev.lumas.shops.components.data.SlotList;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.manager.GsonHolder;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Enumeration;
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
 * <p><b>Layout.</b> Templates live under {@code markets/<namespace>/<value>.json}.
 * States live under {@code markets/<namespace>/states/<value>.state.json}.
 * The {@code namespace} of a {@link Key} becomes its parent directory; {@code value}
 * is the file name (with {@code /} flattened to {@code _}).
 *
 * <p><b>Bootstrap.</b> If the {@code markets/} directory doesn't exist on first run,
 * any bundled {@code markets/} tree in the JAR is copied out as a starting set.
 *
 * <p><b>Caching.</b> Templates and states are cached; invalidate templates via
 * {@link #invalidate(Key)} or {@link #invalidateAll()}.
 */
@NullMarked
public class MarketManager {

    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();
    private static final String STATE_SUFFIX = ".state.json";
    private static final String TEMPLATE_SUFFIX = ".json";
    private static final String STATES_DIR = "states";
    private static final String BUNDLED_RESOURCE_DIR = "markets";

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

    public MarketManager() {
        bootstrapFromJarIfMissing();
    }


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
    public MarketTemplate edit(Key key, @Nullable Component title, @Nullable Integer size, @Nullable List<SlotEntry> staticSlots, @Nullable SlotList contentSlots) {
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
     * @throws IllegalArgumentException if the market is missing or the item is not present
     */
    @SneakyThrows
    public MarketTemplate removeItem(Key marketKey, Key itemKey) {
        MarketTemplate current = requireTemplate(marketKey);
        if (!current.items().containsKey(itemKey)) {
            throw new IllegalArgumentException("Item does not exist in market: " + itemKey);
        }

        Map<Key, MarketItem> updated = new LinkedHashMap<>(current.items());
        updated.remove(itemKey);

        return writeAndCache(withItems(current, updated));
    }


    @SneakyThrows
    public void deleteMarket(Key key) {
        if (!exists(key)) {
            throw new IllegalStateException("No market exists for key: " + key);
        }

        Files.deleteIfExists(templateFileFor(key));
        Files.deleteIfExists(stateFileFor(key));

        templateCache.remove(key);
        stateCache.remove(key);
    }


    /**
     * Walks every {@code <namespace>/} subdirectory and returns the keys of all templates.
     * Skips the per-namespace {@code states/} folder.
     */
    public Set<Key> keys() {
        Set<Key> keys = new HashSet<>();
        if (!Files.isDirectory(directory)) return keys;

        try (Stream<Path> namespaceDirs = Files.list(directory)) {
            namespaceDirs.filter(Files::isDirectory).forEach(namespaceDir -> collectKeys(namespaceDir, keys));
        } catch (IOException e) {
            LOGGER.error("Failed to list markets directory", e);
        }
        return keys;
    }

    private void collectKeys(Path namespaceDir, Set<Key> keys) {
        String namespace = namespaceDir.getFileName().toString();
        try (Stream<Path> files = Files.list(namespaceDir)) {
            files.filter(p -> {
                if (Files.isDirectory(p)) return false;
                String name = p.getFileName().toString();
                return name.endsWith(TEMPLATE_SUFFIX) && !name.endsWith(STATE_SUFFIX);
            }).forEach(file -> {
                Key key = keyFromTemplateFile(namespace, file);
                if (key != null) keys.add(key);
            });
        } catch (IOException e) {
            LOGGER.error("Failed to list namespace directory " + namespaceDir, e);
        }
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


    private void bootstrapFromJarIfMissing() {
        if (Files.exists(directory)) return;

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources(BUNDLED_RESOURCE_DIR);
            if (!resources.hasMoreElements()) {
                Files.createDirectories(directory);
                return;
            }

            Files.createDirectories(directory);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                try (FileSystem fs = "jar".equals(url.getProtocol())
                        ? FileSystems.newFileSystem(url.toURI(), Collections.emptyMap())
                        : null) {

                    Path source = (fs == null)
                            ? Paths.get(url.toURI())
                            : fs.getPath(BUNDLED_RESOURCE_DIR);

                    copyRecursive(source, directory);
                }
            }
            LOGGER.info("Bootstrapped markets directory from bundled defaults");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to bootstrap markets directory from JAR", e);
        }
    }

    private static void copyRecursive(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Files.createDirectories(target.resolve(relativize(source, dir)));
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.copy(file, target.resolve(relativize(source, file)), StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /** Cross-filesystem-safe relativize: walks segments rather than {@code Path.relativize}. */
    private static String relativize(Path base, Path child) {
        Path rel = base.relativize(child);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rel.getNameCount(); i++) {
            if (i > 0) sb.append('/');
            sb.append(rel.getName(i).toString());
        }
        return sb.toString();
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
        Path file = templateFileFor(template.key());
        Files.createDirectories(file.getParent());
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
        Path file = stateFileFor(key);
        try {
            Files.createDirectories(file.getParent());
        } catch (IOException e) {
            LOGGER.error("Failed to create states directory at " + file.getParent(), e);
            return;
        }

        Gson gson = GsonHolder.instance().get();
        try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            gson.toJson(state, MarketState.class, writer);
        } catch (IOException e) {
            LOGGER.error("Failed to save market state " + key, e);
        }
    }

    private Path templateFileFor(Key key) {
        return directory.resolve(key.namespace()).resolve(safeValue(key) + TEMPLATE_SUFFIX);
    }

    private Path stateFileFor(Key key) {
        return directory.resolve(key.namespace()).resolve(STATES_DIR).resolve(safeValue(key) + STATE_SUFFIX);
    }

    /** Flattens {@code /} in a key's value so it becomes a single file name. */
    private String safeValue(Key key) {
        return key.value().replace('/', '_');
    }

    @SuppressWarnings("PatternValidation")
    private @Nullable Key keyFromTemplateFile(String namespace, Path file) {
        String name = file.getFileName().toString();
        if (!name.endsWith(TEMPLATE_SUFFIX)) return null;
        String stem = name.substring(0, name.length() - TEMPLATE_SUFFIX.length());
        String value = stem.replace('_', '/');
        try {
            return Key.key(namespace, value);
        } catch (Exception e) {
            LOGGER.warning("Could not parse market key from filename: " + name);
            return null;
        }
    }
}