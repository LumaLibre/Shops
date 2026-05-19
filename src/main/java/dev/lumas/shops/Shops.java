package dev.lumas.shops;

import lombok.Getter;
import lombok.experimental.Accessors;
import org.bukkit.plugin.java.JavaPlugin;

@Accessors(fluent = true)
public final class Shops extends JavaPlugin {

    @Getter
    private static Shops instance;
}
