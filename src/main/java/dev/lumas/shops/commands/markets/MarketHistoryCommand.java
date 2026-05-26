package dev.lumas.shops.commands.markets;

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
import dev.lumas.shops.components.MarketHistory;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.CollectionUtil;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "history",
        permission = "shops.command.history",
        usage = "/<command> history",
        parent = CommandManager.class
)
public class MarketHistoryCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "key", provider = KeyProvider.class) Key key, @Argument(value = "targets", optional = true) List<Player> targets, @Argument(value = "page", optional = true) @Nullable Integer page) {
        List<Player> finalTargets = CollectionUtil.ofNonNulls(targets);

        if (finalTargets.isEmpty()) {
            if (src.getSender() instanceof Player target) {
                finalTargets.add(target);
            } else {
                Viewers.sendMessage(src.getSender(), "shops.messages.error.player_not_found");
                return;
            }
        }

        MarketTemplate template = MarketManager.INSTANCE.template(key);
        MarketState state = MarketManager.INSTANCE.state(key);

        for (Player player : finalTargets) {
            if (template == null) {
                Viewers.sendMessage(player, "shops.messages.error.no_market");
                return;
            }


            if (state.receipts().isEmpty()) {
                Viewers.sendMessage(player, "shops.messages.error.no_purchases");
                return;
            }

            MarketHistory marketHistory = new MarketHistory(template, state, player.locale());
            marketHistory.open(player);

            if (page != null) {
                marketHistory.setPage(page + 1);
            }
        }
    }

    @Suggests("key")
    public CompletableFuture<Suggestions> suggestKey(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        MarketManager.INSTANCE.keys().stream()
                .map(Key::asString)
                .filter(s -> s.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
