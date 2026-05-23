package dev.lumas.shops.commands.markets;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.components.dialog.CreateMarketDialog;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.SubCommand;
import dev.lumas.shops.util.Viewers;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
@Register(Autowire.SUBCOMMAND)
@CommandMeta(
        name = "edit",
        playerOnly = true,
        parent = CommandManager.class,
        permission = "shops.command.edit",
        usage = "/<command> edit <key>"
)
public class EditMarketCommand implements SubCommand {

    @Override
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Key key = key(args[0]);
        MarketTemplate template = MarketManager.INSTANCE.template(key);
        if (template == null) {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
            return true;
        }

        CreateMarketDialog dialog = new CreateMarketDialog(player.locale(), key);
        dialog.template(template);
        dialog.show(player);
        return true;
    }

    @Override
    public List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return MarketManager.INSTANCE.keys().stream().map(Key::asString).toList();
        }
        return List.of();
    }
}
