package dev.lumas.shops.commands.items;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.shops.Shops;
import dev.lumas.shops.commands.ArgumentFlagReader;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.interfaces.SubCommand;
import dev.lumas.shops.util.Viewers;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@NullMarked
@Register(Autowire.SUBCOMMAND)
@CommandMeta(
        name = "serialize",
        playerOnly = true,
        parent = CommandManager.class,
        permission = "shops.command.serialize",
        usage = "/<command> serialize [-console] [-base64] [-print]"
)
public class SerializeItemCommand implements SubCommand {

    @Override
    public boolean execute(Shops plugin, CommandSender sender, String label, String[] args) {
        Player player = (Player) sender;
        ItemStack item = player.getInventory().getItemInMainHand();
        ArgumentFlagReader flagReader = new ArgumentFlagReader(args);

        String serialized = flagReader.getFlagValueAsBoolean("base64") ?
                Base64.getEncoder().encodeToString(item.serializeAsBytes()) :
                Bukkit.getUnsafe().serializeItemAsJson(item).getAsJsonObject().toString();

        if (flagReader.getFlagValueAsBoolean("console")) {
            Viewers.sendMessage(Bukkit.getConsoleSender(), Component.text(serialized));
        }

        boolean print = flagReader.getFlagValueAsBoolean("print");
        Component message = MiniMessage.miniMessage().deserialize(print ? serialized : "<b><dark_green>[Serialized Item]")
                .hoverEvent(Component.text(!print ? serialized : "Click to copy"))
                .clickEvent(ClickEvent.copyToClipboard(serialized));

        if (print) {
            player.sendMessage(message); // omit prefix
        } else {
            Viewers.sendMessage(player, message);
        }
        return true;
    }

    @Override
    public @Nullable List<String> tabComplete(Shops plugin, CommandSender sender, String[] args) {
        List<String> argList = List.of(args);
        List<String> result = new ArrayList<>();
        add(argList, result, "-console");
        add(argList, result, "-base64");
        add(argList, result, "-print");

        if (argList.getLast().startsWith("-")) {
            result.addAll(List.of("true", "false"));
        }
        return result;
    }

    private void add(List<String> args, List<String> mutableList, String arg) {
        if (!args.contains(arg)) {
            mutableList.add(arg);
        }
    }
}
