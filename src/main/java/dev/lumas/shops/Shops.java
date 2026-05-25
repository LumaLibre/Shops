package dev.lumas.shops;

import dev.lumas.core.manager.Modules;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.InventoryUtil;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.plugin.java.JavaPlugin;

@Accessors(fluent = true)
public final class Shops extends JavaPlugin {

    @Getter
    private static Shops instance;
    private static Modules modules;

    @Override
    public void onLoad() {
        instance = this;
        modules = new Modules(this);
    }

    @Override
    public void onEnable() {
        modules.register();
        MarketManager.INSTANCE.bootstrapFromJarIfMissing();
    }

    @Override
    public void onDisable() {
        MarketManager.INSTANCE.shutdown();
        modules.unregister();

        try {
            InventoryUtil.closeAllMarkets();
        } catch (Exception ignored) {
        }
    }
}
