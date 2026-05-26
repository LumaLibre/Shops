package dev.lumas.shops.commands.markets;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.commands.providers.KeyProvider;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "delete",
        permission = "shops.command.delete",
        usage = "/<command> delete <key> -confirm",
        parent = CommandManager.class
)
public class DeleteMarketCommand implements BrigadierSubCommand {
    // Using DSL for '-confirm'
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> buildTree(LiteralArgumentBuilder<CommandSourceStack> builder, Commands commands) {
        return builder.then(Commands.argument("key", new KeyProvider().provide())
                .suggests((ctx, suggestions) -> {
                    String remaining = suggestions.getRemaining().toLowerCase();
                    MarketManager.INSTANCE.keys().stream()
                            .map(Key::asString)
                            .filter(s -> s.toLowerCase().startsWith(remaining))
                            .forEach(suggestions::suggest);
                    return suggestions.buildFuture();
                })
                .executes(ctx -> {
                    Viewers.sendMessage(ctx.getSource().getSender(), "shops.messages.confirm");
                    return 0;
                })
                .then(Commands.literal("-confirm")
                        .executes(ctx -> {
                            CommandSender sender = ctx.getSource().getSender();
                            Key key = ctx.getArgument("key", Key.class);
                            try {
                                MarketManager.INSTANCE.deleteMarket(key);
                                Viewers.sendMessage(sender, "shops.messages.delete.success", key);
                            } catch (IllegalArgumentException _) {
                                Viewers.sendMessage(sender, "shops.messages.error.no_market");
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}