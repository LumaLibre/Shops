package dev.lumas.shops.config;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.manager.Services;
import dev.lumas.core.model.Service;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.Shops;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;


/**
 * Loads .lang.properties files, syncs JAR defaults with user-edited copies on disk,
 * and serves translations to Adventure's GlobalTranslator.
 *
 * Files are named "<language-tag>.lang.properties", e.g. "en-US.lang.properties".
 * Keys use MiniMessage syntax: "shops.greet=<green>Hello, <arg:0>!"
 */
@Register(Autowire.SERVICE)
public class TranslatorService extends MiniMessageTranslator implements Service {

    private static final String LANG_SUFFIX = ".lang.properties";
    private static final Key TRANSLATOR_KEY = Key.key("shops", "translator");
    private static final PluginContextLogger LOGGER = PluginContextLogger.getPluginLogger();

    private final Locale defaultLocale;
    private final boolean clientSideTranslations;

    private final File localeDirectory = Shops.instance().getDataPath().resolve("locale").toFile();
    private Map<Locale, Properties> translations = Collections.emptyMap();

    public TranslatorService() {
        ShopsConfig config = ShopsConfig.instance();
        this.defaultLocale = Locale.forLanguageTag(config.locale());
        this.clientSideTranslations = config.clientSideTranslations();
    }

    public void reload() {
        syncLangFiles();
        loadLangFiles();
    }

    @Override
    public void register() {
        reload();
        GlobalTranslator.translator().addSource(this);
    }

    @Override
    public void unregister() {
        GlobalTranslator.translator().removeSource(this);
    }


    /**
     * For each .lang.properties bundled in the JAR's /locale/ folder:
     *  - if the user has no copy on disk, copy it out verbatim.
     *  - if they do, sync key sets without rewriting unchanged lines.
     */
    private void syncLangFiles() {
        if (!localeDirectory.exists() && !localeDirectory.mkdirs()) {
            throw new IllegalStateException(
                    "Failed to create locale directory: " + localeDirectory.getAbsolutePath());
        }

        try {
            Enumeration<URL> resources = getClass().getClassLoader().getResources("locale");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                // /locale/ can be either inside a JAR (jar: protocol) or a real
                // directory on disk (during dev). Handle both.
                try (FileSystem fs = "jar".equals(url.getProtocol())
                        ? FileSystems.newFileSystem(url.toURI(), Collections.emptyMap())
                        : null) {

                    Path internalDir = (fs == null)
                            ? Paths.get(url.toURI())
                            : fs.getPath("locale");

                    try (DirectoryStream<Path> stream =
                                 Files.newDirectoryStream(internalDir, "*" + LANG_SUFFIX)) {
                        for (Path path : stream) {
                            syncOne(path);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to sync language files", e);
        }
    }

    /**
     * Sync a single bundled file with its external counterpart.
     *
     * <p>If no external file exists, the bundled file is copied verbatim. Otherwise,
     * the diff between JAR keys and user keys is applied as a line-level edit:
     *
     * <ul>
     *   <li>Lines belonging to orphan keys (in user, not in JAR) are dropped.</li>
     *   <li>Lines for new keys (in JAR, not in user) are appended at the bottom,
     *       using the JAR's raw line text so escaping is preserved.</li>
     * </ul>
     *
     * If the diff is empty, the file is not touched at all.
     */
    private void syncOne(Path internalFile) throws IOException {
        String fileName = internalFile.getFileName().toString();
        Path externalFile = localeDirectory.toPath().resolve(fileName);

        if (!Files.exists(externalFile)) {
            Files.copy(internalFile, externalFile, StandardCopyOption.COPY_ATTRIBUTES);
            return;
        }

        Properties jarProps = loadProps(internalFile);
        Properties userProps = loadProps(externalFile);

        Set<String> jarKeys = jarProps.stringPropertyNames();
        Set<String> userKeys = userProps.stringPropertyNames();

        Set<String> toAdd = new HashSet<>(jarKeys);
        toAdd.removeAll(userKeys);

        Set<String> toRemove = new HashSet<>(userKeys);
        toRemove.removeAll(jarKeys);

        if (toAdd.isEmpty() && toRemove.isEmpty()) {
            return;
        }

        // Read both files as raw lines so we can preserve user formatting and
        // pull verbatim text for new keys from the JAR.
        List<String> userLines = Files.readAllLines(externalFile, StandardCharsets.UTF_8);
        List<String> jarLines = Files.readAllLines(internalFile, StandardCharsets.UTF_8);

        List<String> output = new ArrayList<>(userLines.size() + toAdd.size());

        // Walk the user file, dropping logical-line groups whose key is in toRemove.
        // A logical line may span multiple physical lines via trailing backslash continuation.
        int i = 0;
        while (i < userLines.size()) {
            int start = i;
            while (i < userLines.size() && endsWithContinuation(userLines.get(i))) {
                i++;
            }
            if (i < userLines.size()) i++;

            List<String> group = userLines.subList(start, i);
            String key = extractKey(group);
            if (key != null && toRemove.contains(key)) {
                continue;
            }
            output.addAll(group);
        }

        // Append new keys from JAR, pulling raw line groups verbatim.
        if (!toAdd.isEmpty()) {
            if (!output.isEmpty() && !output.getLast().isEmpty()) {
                output.add("");
            }
            output.add("# Added by Shops " + Shops.instance().getPluginMeta().getVersion() + ". review and customize these new keys.");

            int j = 0;
            while (j < jarLines.size()) {
                int gStart = j;
                while (j < jarLines.size() && endsWithContinuation(jarLines.get(j))) {
                    j++;
                }
                if (j < jarLines.size()) j++;

                List<String> group = jarLines.subList(gStart, j);
                String key = extractKey(group);
                if (key != null && toAdd.contains(key)) {
                    output.addAll(group);
                }
            }
        }

        // Write atomically so a crash mid-write doesn't corrupt the user's file.
        Path tmp = externalFile.resolveSibling(fileName + ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(tmp, StandardCharsets.UTF_8)) {
            for (String line : output) {
                writer.write(line);
                writer.write('\n');
            }
        }
        Files.move(tmp, externalFile,
                StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);

        LOGGER.info("Synced " + fileName + ": +" + toAdd.size() + " key(s), -" + toRemove.size() + " key(s)");
    }

    private static Properties loadProps(Path file) throws IOException {
        Properties props = new Properties();
        try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            props.load(reader);
        }
        return props;
    }

    /**
     * @return the key declared on the first non-blank, non-comment line of the group, or null
     */
    private static @Nullable String extractKey(List<String> group) {
        if (group.isEmpty()) return null;
        String head = group.get(0);
        String trimmed = head.stripLeading();
        if (trimmed.isEmpty()) return null;
        char c = trimmed.charAt(0);
        if (c == '#' || c == '!') return null;

        StringBuilder key = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < trimmed.length(); i++) {
            char ch = trimmed.charAt(i);
            if (escaped) {
                key.append(ch);
                escaped = false;
                continue;
            }
            if (ch == '\\') {
                escaped = true;
                continue;
            }
            if (ch == '=' || ch == ':' || Character.isWhitespace(ch)) break;
            key.append(ch);
        }
        return decodeKeyEscapes(key.toString());
    }

    private static String decodeKeyEscapes(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case 'u' -> {
                        if (i + 4 < s.length()) {
                            try {
                                out.append((char) Integer.parseInt(s.substring(i + 1, i + 5), 16));
                                i += 4;
                            } catch (NumberFormatException ignored) {
                                out.append(next);
                            }
                        } else {
                            out.append(next);
                        }
                    }
                    default -> out.append(next);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static boolean endsWithContinuation(String line) {
        int trailingBackslashes = 0;
        for (int i = line.length() - 1; i >= 0 && line.charAt(i) == '\\'; i--) {
            trailingBackslashes++;
        }
        return trailingBackslashes % 2 == 1;
    }


    private void loadLangFiles() {
        File[] files = localeDirectory.listFiles(f -> f.getName().endsWith(LANG_SUFFIX));
        if (files == null) {
            throw new IllegalStateException("Locale directory is not a directory!");
        }

        Map<Locale, Properties> loaded = new HashMap<>();
        for (File file : files) {
            try (InputStream in = new FileInputStream(file);
                 Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {

                Properties props = new Properties();
                props.load(reader);

                String tag = file.getName().substring(
                        0, file.getName().length() - LANG_SUFFIX.length());
                Locale locale = Locale.forLanguageTag(tag);
                loaded.put(locale, props);

            } catch (IOException e) {
                LOGGER.warning("Failed to load lang file: " + file.getName(), e);
            }
        }

        this.translations = Map.copyOf(loaded);

        if (!translations.containsKey(defaultLocale)) {
            throw new IllegalStateException(
                    "Default locale " + defaultLocale.toLanguageTag() + " not found in lang files");
        }
    }



    @Override
    public @NotNull Key name() {
        return TRANSLATOR_KEY;
    }

    @Override
    public @Nullable String getMiniMessageString(@NotNull String key, @NotNull Locale locale) {
        Properties props = null;

        if (clientSideTranslations) {
            props = translations.get(locale);
            if (props == null || !props.containsKey(key)) {
                Properties langOnly = translations.get(Locale.forLanguageTag(locale.getLanguage()));
                if (langOnly != null && langOnly.containsKey(key)) {
                    props = langOnly;
                }
            }
        }

        // fall back to server default
        if (props == null || !props.containsKey(key)) {
            props = translations.get(defaultLocale);
        }

        return props == null ? null : props.getProperty(key);
    }

    public static TranslatorService instance() {
        return (TranslatorService) Services.getTracked(TranslatorService.class);
    }
}