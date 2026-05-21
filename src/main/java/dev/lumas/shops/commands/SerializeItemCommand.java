package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.components.backing.ItemStackCodec;
import dev.lumas.shops.gson.GsonHolder;
import dev.lumas.shops.interfaces.SubCommand;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@Register(Autowire.SUBCOMMAND)
@CommandMeta(name = "serialize", playerOnly = true, parent = CommandManager.class)
@NullMarked
public class SerializeItemCommand implements SubCommand {

    @Override
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        //String base64 = ItemStackCodec.INSTANCE.toJson(item);
        var jsonObject = Bukkit.getUnsafe().serializeItemAsJson(item);
        player.sendMessage(jsonObject.getAsJsonObject().toString());
        System.out.println(jsonObject.getAsJsonObject());
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        return null;
    }
}
