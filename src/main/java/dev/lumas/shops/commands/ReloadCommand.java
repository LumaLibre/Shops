package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.config.TranslatorService;
import dev.lumas.shops.interfaces.SubCommand;
import dev.lumas.shops.util.InventoryUtil;
import dev.lumas.shops.util.Viewers;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
@Register(Autowire.SUBCOMMAND)
@CommandMeta(
        name = "reload",
        parent = CommandManager.class,
        permission = "shops.command.reload",
        usage = "/<command> reload [key]"
)
public class ReloadCommand implements SubCommand {

    @Override
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        if (args.length == 0) {
            MarketManager.INSTANCE.invalidateAll();
            TranslatorService.instance().reload();
            // TODO: Reload config
            Viewers.sendMessage(sender, "shops.messages.reloaded");
            InventoryUtil.closeAllMarkets();
        } else {
            Key key = key(args[0]);
            MarketManager.INSTANCE.invalidate(key);
            Viewers.sendMessage(sender, "shops.messages.reloaded.market", key);
            InventoryUtil.closeAllMarkets(key);
        }
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return MarketManager.INSTANCE.keys().stream().map(Key::asString).toList();
        }
        return List.of();
    }
}
