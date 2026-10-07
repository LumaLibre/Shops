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
import dev.lumas.shops.components.data.PurchaseReceipt;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "receipt",
        permission = "shops.command.receipt",
        usage = "/<command> receipt <marketKey> <itemKey> <amount> [player]",
        parent = CommandManager.class
)
public class ReceiptCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src,
                    @Argument(value = "marketKey", provider = KeyProvider.class) Key marketKey,
                    @Argument(value = "itemKey", provider = KeyProvider.class) Key itemKey,
                    @Argument("amount") Integer amount,
                    @Argument(value = "target", optional = true) @Nullable String target) {
        CommandSender sender = src.getSender();
        OfflinePlayer player = target != null ? resolvePlayer(target) : sender instanceof Player self ? self : null;
        if (player == null) {
            Viewers.sendMessage(sender, "shops.messages.error.player_not_found");
            return;
        }

        MarketTemplate template = MarketManager.INSTANCE.template(marketKey);
        if (template == null) {
            Viewers.sendMessage(sender, "shops.messages.error.no_market");
            return;
        }
        if (!template.items().containsKey(itemKey)) {
            Viewers.sendMessage(sender, "shops.messages.error.no_item", marketKey);
            return;
        }

        int finalAmount = Math.max(0, amount);
        MarketState state = MarketManager.INSTANCE.state(marketKey);
        state.setPurchases(PurchaseReceipt.of(player.getUniqueId(), itemKey), finalAmount);

        String name = player.getName() != null ? player.getName() : player.getUniqueId().toString();
        Viewers.sendMessage(sender, "shops.messages.purchases.set", name, itemKey, finalAmount);
    }

    private static @Nullable OfflinePlayer resolvePlayer(String input) {
        try {
            return Bukkit.getOfflinePlayer(UUID.fromString(input));
        } catch (IllegalArgumentException ignored) {
        }
        Player online = Bukkit.getPlayerExact(input);
        return online != null ? online : Bukkit.getOfflinePlayerIfCached(input);
    }

    @Suggests("target")
    public CompletableFuture<Suggestions> suggestTarget(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase();
        Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(s -> s.toLowerCase().startsWith(remaining))
                .forEach(builder::suggest);
        return builder.buildFuture();
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
