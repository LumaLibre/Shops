package dev.lumas.shops.commands.items;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.components.MarketManager;
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
        name = "delitem",
        playerOnly = true,
        parent = CommandManager.class,
        permission = "shops.command.removeitem",
        usage = "/<command> delitem <marketKey> <itemKey>"
)
public class RemoveItemCommand implements SubCommand {

    @Override
    @SuppressWarnings("PatternValidation")
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Key marketKey = Key.key(args[0]);
        Key itemKey = Key.key(args[1]);
        MarketTemplate template = MarketManager.INSTANCE.template(marketKey);

        if (template == null) {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
            return true;
        }

        try {
            MarketManager.INSTANCE.removeItem(marketKey, itemKey);
            Viewers.sendMessage(player, "shops.messages.delete.item.success", itemKey, marketKey);
        } catch (IllegalArgumentException _) {
            Viewers.sendMessage(player, "shops.messages.error.no_item");
        }
        return true;
    }

    @Override
    public List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        if (args.length == 1) {
            return MarketManager.INSTANCE.keys().stream().map(Key::asString).toList();
        } else if (args.length == 2) {
            MarketTemplate template = MarketManager.INSTANCE.template(Key.key(args[0]));
            if (template != null) {
                return template.items().keySet().stream().map(Key::asString).toList();
            }
        }
        return List.of();
    }
}
