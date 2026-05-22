package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.MarketManager;
import dev.lumas.shops.components.dialog.AddMarketItemDialog;
import dev.lumas.shops.components.dialog.CreateMarketDialog;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.SubCommand;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Register(Autowire.SUBCOMMAND)
@CommandMeta(name = "additem", playerOnly = true, parent = CommandManager.class)
@NullMarked
public class AddItemCommand implements SubCommand {

    @Override
    @SuppressWarnings("PatternValidation")
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir()) {
            throw new UnsupportedOperationException("Not yet implemented.");
        }
        Key key = Key.key(args[0]);
        MarketTemplate template = MarketManager.INSTANCE.template(key);

        if (template == null) {
            throw new UnsupportedOperationException("Not yet implemented.");
        }


        AddMarketItemDialog dialog = new AddMarketItemDialog(player.locale(), template, itemStack);
        dialog.show(player);
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        return MarketManager.INSTANCE.keys().stream().map(Key::asString).toList();
    }
}
