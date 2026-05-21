package dev.lumas.shops.config;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.manager.Services;
import dev.lumas.core.model.Service;
import dev.lumas.core.util.PluginContextLogger;
import dev.lumas.shops.Shops;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslator;
import net.kyori.adventure.translation.GlobalTranslator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;


/**
 * Loads .lang.properties files, merges JAR defaults with user-edited copies on disk,
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
        this.defaultLocale = Locale.forLanguageTag("en-US");
        this.clientSideTranslations = true;
    }

    public TranslatorService(Locale defaultLocale, boolean clientSideTranslations) {
        this.defaultLocale = defaultLocale;
        this.clientSideTranslations = clientSideTranslations;
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
     *  - if the user has no copy on disk, copy it out
     *  - if they do, merge: user values win, missing keys filled from JAR
     * Result is written back to disk so users see new keys after updates.
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
                            mergeAndStore(path);
                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException("Failed to sync language files", e);
        }
    }

    private void mergeAndStore(Path internalFile) throws IOException {
        String fileName = internalFile.getFileName().toString();
        File externalFile = new File(localeDirectory, fileName);

        Properties internalProps = new Properties();
        try (Reader reader = Files.newBufferedReader(internalFile, StandardCharsets.UTF_8)) {
            internalProps.load(reader);
        }

        Properties merged = new Properties();
        if (externalFile.exists()) {
            Properties externalProps = new Properties();
            try (Reader reader = new InputStreamReader(
                    new FileInputStream(externalFile), StandardCharsets.UTF_8)) {
                externalProps.load(reader);
            }
            // user's values take precedence; only fill in missing keys from JAR
            merged.putAll(externalProps);
            for (String key : internalProps.stringPropertyNames()) {
                merged.putIfAbsent(key, internalProps.getProperty(key));
            }
        } else {
            merged.putAll(internalProps);
            if (!externalFile.createNewFile()) {
                throw new IOException("Could not create file: " + externalFile);
            }
        }

        try (Writer writer = new OutputStreamWriter(
                new FileOutputStream(externalFile), StandardCharsets.UTF_8)) {
            writeSorted(merged, writer);
        }
    }

    /** Properties.store() adds a timestamp comment we don't want; write our own. */
    private void writeSorted(Properties props, Writer writer) throws IOException {
        List<String> keys = new ArrayList<>(props.stringPropertyNames());
        Collections.sort(keys);
        for (String key : keys) {
            writer.write(key + "=" + props.getProperty(key) + "\n");
        }
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
            // try exact locale first (e.g. en-US)
            props = translations.get(locale);
            // then language only (e.g. en) — graceful fallback for en-GB → en
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