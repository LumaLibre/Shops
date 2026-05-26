package dev.lumas.shops.commands;

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
import dev.lumas.shops.commands.providers.KeyProvider;
import dev.lumas.shops.config.ShopsConfig;
import dev.lumas.shops.config.TranslatorService;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.InventoryUtil;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "reload",
        parent = CommandManager.class,
        permission = "shops.command.reload",
        usage = "/<command> reload [key]"
)
public class ReloadCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "key", optional = true, provider = KeyProvider.class) @Nullable Key key) {
        CommandSender sender = src.getSender();
        if (key == null) {
            MarketManager.INSTANCE.invalidateAll();
            TranslatorService.instance().reload();
            ShopsConfig.MEMORIZED.reload();
            Viewers.sendMessage(sender, "shops.messages.reloaded");
            InventoryUtil.closeAllMarkets();
        } else {
            MarketManager.INSTANCE.invalidate(key);
            Viewers.sendMessage(sender, "shops.messages.reloaded.market", key);
            InventoryUtil.closeAllMarkets(key);
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