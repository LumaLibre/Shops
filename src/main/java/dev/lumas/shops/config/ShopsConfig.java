package dev.lumas.shops.config;

import dev.lumas.shops.Shops;
import dev.lumas.shops.util.Lazy;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.nio.file.Path;

@Getter
@ConfigSerializable
@Accessors(fluent = true)
@SuppressWarnings("FieldMayBeFinal")
public class ShopsConfig {

    private static final Lazy<YamlConfigurationLoader> LOADER = Lazy.of(() -> {
        Path file = Shops.instance().getDataPath().resolve("config.yml");
        return YamlConfigurationLoader.builder()
                .path(file)
                .nodeStyle(NodeStyle.BLOCK)
                .indent(2)
                .build();
    });

    public static final Lazy.Memorized<ShopsConfig> MEMORIZED = Lazy.memorized(() -> {
        YamlConfigurationLoader loader = LOADER.get();
        try {
            CommentedConfigurationNode root = loader.load();
            ShopsConfig loaded = root.get(ShopsConfig.class, new ShopsConfig());

            // Write back so new fields appear in existing files with defaults + comments
            root.set(ShopsConfig.class, loaded);
            loader.save(root);
            return loaded;
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
    });

    public static ShopsConfig instance() {
        return MEMORIZED.get();
    }

    // Config options

    @Comment("The locale to use for messages.")
    @Setting("locale")
    private String locale = "en-US";

    @Comment("Whether to use client-side translations.")
    @Setting("client-side-translations")
    private boolean clientSideTranslations = true;

    @Comment("The default namespace to use.")
    @Setting("default-namespace")
    private String defaultNamespace = "shops";

    @Comment("Whether to close the inventory after a successful purchase.")
    @Setting("close-after-purchase")
    private boolean closeAfterPurchase = false;

    @Comment("Whether to open the market after adding an item to it.")
    @Setting("open-after-adding-item")
    private boolean openAfterAddingItem = false;

    @Comment("The maximum amount of items that can be purchased at once.")
    @Setting("max-purchase-amount")
    private int maxPurchaseAmount = 10;

}
