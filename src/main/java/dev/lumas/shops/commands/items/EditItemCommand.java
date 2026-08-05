package dev.lumas.shops.commands.items;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.commands.providers.KeyProvider;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.dialog.AddMarketItemDialog;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "edititem",
        playerOnly = true,
        permission = "shops.command.edititem",
        usage = "/<command> edititem <marketKey> <itemKey> [-quick]",
        parent = CommandManager.class
)
public class EditItemCommand implements BrigadierSubCommand {

    // Using DSL for '-quick'
    @Override
    public LiteralArgumentBuilder<CommandSourceStack> buildTree(LiteralArgumentBuilder<CommandSourceStack> builder, Commands commands) {
        return builder.then(Commands.argument("marketKey", new KeyProvider().provide())
                .suggests((ctx, suggestions) -> {
                    String remaining = suggestions.getRemaining().toLowerCase();
                    MarketManager.INSTANCE.keys().stream()
                            .map(Key::asString)
                            .filter(s -> s.toLowerCase().startsWith(remaining))
                            .forEach(suggestions::suggest);
                    return suggestions.buildFuture();
                })
                .then(Commands.argument("itemKey", new KeyProvider().provide())
                        .suggests((ctx, suggestions) -> {
                            MarketTemplate template = MarketManager.INSTANCE.template(ctx.getArgument("marketKey", Key.class));
                            if (template == null) {
                                return suggestions.buildFuture();
                            }

                            String remaining = suggestions.getRemaining().toLowerCase();
                            template.items().keySet().stream()
                                    .map(Key::asString)
                                    .filter(s -> s.toLowerCase().startsWith(remaining))
                                    .forEach(suggestions::suggest);
                            return suggestions.buildFuture();
                        })
                        .executes(ctx -> run(ctx, false))
                        .then(Commands.literal("-quick")
                                .executes(ctx -> run(ctx, true))
                        )
                )
        );
    }


    private int run(CommandContext<CommandSourceStack> ctx, boolean quick) {
        Player player = (Player) ctx.getSource().getSender();
        Key marketKey = ctx.getArgument("marketKey", Key.class);
        Key itemKey = ctx.getArgument("itemKey", Key.class);

        MarketTemplate template = MarketManager.INSTANCE.template(marketKey);
        if (template == null) {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
            return 0;
        }

        MarketItem existing = template.item(itemKey);
        if (existing == null) {
            Viewers.sendMessage(player, "shops.messages.error.no_item", marketKey);
            return 0;
        }

        ItemStack inHand = player.getInventory().getItemInMainHand().clone();

        if (quick) {
            // Nothing in hand means there's no new display item, so there's nothing to do.
            if (inHand.isEmpty()) {
                Viewers.sendMessage(player, "shops.messages.error.bad_item");
                return 0;
            }

            MarketItem updated = new MarketItem(itemKey, existing.stock(), existing.currency(), existing.product(), inHand);
            MarketManager.INSTANCE.replaceItem(marketKey, updated, template.indexOf(itemKey));
            Viewers.sendMessage(player, "shops.messages.edit.item.success", itemKey, marketKey);
            return Command.SINGLE_SUCCESS;
        }

        AddMarketItemDialog dialog = new AddMarketItemDialog(player.locale(), template, inHand.isEmpty() ? existing.stack() : inHand);
        dialog.editing(existing);
        dialog.show(player);
        return Command.SINGLE_SUCCESS;
    }
}
