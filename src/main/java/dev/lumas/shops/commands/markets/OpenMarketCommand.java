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
import dev.lumas.shops.components.Market;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.CollectionUtil;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "market",
        aliases = "open",
        permission = "shops.command.market",
        usage = "/<command> market <key> [target]",
        parent = CommandManager.class
)
public class OpenMarketCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "key", provider = KeyProvider.class) Key key, @Argument(value = "targets", optional = true) @Nullable List<Player> targets) {
        CommandSender sender = src.getSender();

        List<Player> finalTargets = CollectionUtil.ofNonNulls(targets);

        if (finalTargets.isEmpty()) {
            if (sender instanceof Player target) {
                finalTargets.add(target);
            } else {
                Viewers.sendMessage(sender, "shops.messages.error.player_not_found");
                return;
            }
        }

        for (Player player : finalTargets) {
            Market market = MarketManager.INSTANCE.market(key, player.locale());
            if (market != null) {
                market.open(player);
            } else {
                Viewers.sendMessage(player, "shops.messages.error.no_market");
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