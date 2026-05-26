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
import dev.lumas.shops.components.dialog.CreateMarketDialog;
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
        name = "clone",
        playerOnly = true,
        parent = CommandManager.class,
        permission = "shops.command.clone",
        usage = "/<command> clone <key> <newKey>"
)
public class CloneMarketCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "key", provider = KeyProvider.class) Key key, @Argument(value = "newKey", provider = KeyProvider.class) Key newKey) {
        Player player = (Player) src.getSender();
        MarketTemplate template = MarketManager.INSTANCE.template(key);

        if (template != null) {
            CreateMarketDialog dialog = new CreateMarketDialog(player.locale(), newKey);
            dialog.template(template);
            dialog.show(player);
        } else {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
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