package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.dialog.CreateMarketDialog;
import dev.lumas.shops.interfaces.SubCommand;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Register(Autowire.SUBCOMMAND)
@CommandMeta(name = "create", playerOnly = true, parent = CommandManager.class)
@NullMarked
public class CreateMarketCommand implements SubCommand {

    @Override
    @SuppressWarnings("PatternValidation")
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        String name = args[0];

        Key key;

        if (name.contains(":")){
            key = Key.key(name);
        } else {
            key = Key.key("shops:" + name);
        }

        CreateMarketDialog dialog = new CreateMarketDialog(player.locale(), key);
        dialog.show(player);
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        return List.of();
    }
}
