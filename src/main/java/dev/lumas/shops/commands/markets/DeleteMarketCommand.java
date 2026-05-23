package dev.lumas.shops.commands.markets;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.commands.ArgumentFlagReader;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.interfaces.SubCommand;
import dev.lumas.shops.util.Viewers;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
@Register(Autowire.SUBCOMMAND)
@CommandMeta(
        name = "delete",
        parent = CommandManager.class,
        permission = "shops.command.delete",
        usage = "/<command> delete <key> -confirm"
)
public class DeleteMarketCommand implements SubCommand {

    @Override
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        ArgumentFlagReader flagReader = new ArgumentFlagReader(args);
        Key key = key(args[0]);

        if (flagReader.getFlagValueAsBoolean("confirm")) {
            MarketManager.INSTANCE.deleteMarket(key);
            Viewers.sendMessage(sender, "shops.messages.delete.success");
            return true;
        } else {
            Viewers.sendMessage(sender, "shops.messages.confirm");
            return false;
        }
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return MarketManager.INSTANCE.keys().stream().map(Key::asString).toList();
        }
        return List.of();
    }
}
