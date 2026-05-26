package dev.lumas.shops.commands.items;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.BrigadierExecutor;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.annotation.Suggests;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.commands.providers.KeyProvider;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "delitem",
        playerOnly = true,
        permission = "shops.command.removeitem",
        usage = "/<command> delitem <marketKey> <itemKey>",
        parent = CommandManager.class
)
public class RemoveItemCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "marketKey", provider = KeyProvider.class) Key marketKey, @Argument(value = "itemKey", provider = KeyProvider.class) Key itemKey) {
        Player player = (Player) src.getSender();
        MarketTemplate template = MarketManager.INSTANCE.template(marketKey);

        if (template == null) {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
            return;
        }

        try {
            MarketManager.INSTANCE.removeItem(marketKey, itemKey);
            Viewers.sendMessage(player, "shops.messages.delete.item.success", itemKey, marketKey);
        } catch (IllegalArgumentException ignored) {
            Viewers.sendMessage(player, "shops.messages.error.no_item");
        }
    }

    @Suggests("marketKey")
    public CompletableFuture<Suggestions> suggestMarketKey(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        MarketManager.INSTANCE.keys().stream()
                .map(Key::asString)
                .filter(s -> s.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    @Suggests("itemKey")
    public CompletableFuture<Suggestions> suggestItemKey(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        Key marketKey = ctx.getArgument("marketKey", Key.class);
        MarketTemplate template = MarketManager.INSTANCE.template(marketKey);
        if (template == null) {
            return builder.buildFuture();
        }

        String remaining = builder.getRemaining().toLowerCase();
        template.items().keySet().stream()
                .map(Key::asString)
                .filter(s -> s.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}