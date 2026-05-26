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
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.commands.providers.KeyProvider;
import dev.lumas.shops.components.dialog.CreateMarketDialog;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.SubCommand;
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
        name = "edit",
        playerOnly = true,
        parent = CommandManager.class,
        permission = "shops.command.edit",
        usage = "/<command> edit <key>"
)
public class EditMarketCommand implements SubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "key", provider = KeyProvider.class) Key key) {
        Player player = (Player) src.getSender();
        MarketTemplate template = MarketManager.INSTANCE.template(key);
        if (template == null) {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
            return;
        }

        CreateMarketDialog dialog = new CreateMarketDialog(player.locale(), key);
        dialog.template(template);
        dialog.show(player);
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
