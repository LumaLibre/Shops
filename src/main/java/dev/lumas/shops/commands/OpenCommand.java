package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.interfaces.SubCommand;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Register(Autowire.SUBCOMMAND)
@CommandMeta(name = "market", playerOnly = true, parent = CommandManager.class)
@NullMarked
public class OpenCommand implements SubCommand {
    @Override
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        String rawKey = args[0];
        Key key = Key.key(rawKey);

        Market market = MarketManager.INSTANCE.market(key);
        market.open(player);


        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        return MarketManager.INSTANCE.keys().stream().map(Key::asString).toList();
    }
}
