package dev.lumas.shops.commands.markets;

import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.BrigadierExecutor;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.commands.providers.KeyProvider;
import dev.lumas.shops.components.dialog.CreateMarketDialog;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "create",
        playerOnly = true,
        permission = "shops.command.create",
        usage = "/<command> create <key>",
        parent = CommandManager.class
)
public class CreateMarketCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "key", provider = KeyProvider.class) Key key) {
        Player player = (Player) src.getSender();

        CreateMarketDialog dialog = new CreateMarketDialog(player.locale(), key);
        dialog.show(player);
    }

}
