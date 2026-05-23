package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.SubCommand;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@Register(Autowire.SUBCOMMAND)
@CommandMeta(name = "delitem", playerOnly = true, parent = CommandManager.class)
@NullMarked
public class DelItemCommand implements SubCommand {

    @Override
    @SuppressWarnings("PatternValidation")
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        Key key = Key.key(args[0]);
        Key itemKey = Key.key(args[1]);
        MarketTemplate template = MarketManager.INSTANCE.template(key);

        if (template == null) {
            throw new UnsupportedOperationException("Not yet implemented.");
        }

        MarketManager.INSTANCE.removeItem(key, itemKey);
        player.sendMessage(Component.translatable("shops.messages.delete.item.success", Component.text(itemKey.asString()), Component.text(key.asString())));
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
