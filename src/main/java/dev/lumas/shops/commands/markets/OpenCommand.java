package dev.lumas.shops.commands.markets;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.components.Market;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.interfaces.SubCommand;
import dev.lumas.shops.util.Viewers;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
@Register(Autowire.SUBCOMMAND)
@CommandMeta(
        name = "market",
        aliases = "open",
        parent = CommandManager.class,
        permission = "shops.command.market",
        usage = "/<command> market <key>"
)
public class OpenCommand implements SubCommand {
    @Override
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Key key = key(args[0]);

        Player player;
        if (args.length == 2) {
            player = Bukkit.getPlayerExact(args[1]);
            if (player == null) {
                Viewers.sendMessage(sender, "shops.messages.error.player_not_found");
                return true;
            }
        } else {
            player = (Player) sender;
        }

        Market market = MarketManager.INSTANCE.market(key);
        if (market != null) {
            market.open(player);
        } else {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
        }
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return MarketManager.INSTANCE.keys().stream().map(Key::asString).toList();
        } else if (args.length == 2) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
