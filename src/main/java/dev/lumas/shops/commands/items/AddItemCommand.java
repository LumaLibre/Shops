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
import dev.lumas.shops.components.dialog.AddMarketItemDialog;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.concurrent.CompletableFuture;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "additem",
        playerOnly = true,
        permission = "shops.command.additem",
        usage = "/<command> additem <marketKey>",
        parent = CommandManager.class
)
public class AddItemCommand implements BrigadierSubCommand {

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "marketKey", provider = KeyProvider.class) Key marketKey) {
        Player player = (Player) src.getSender();
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir()) {
            Viewers.sendMessage(player, "shops.messages.error.bad_item");
            return;
        }
        MarketTemplate template = MarketManager.INSTANCE.template(marketKey);
        if (template == null) {
            Viewers.sendMessage(player, "shops.messages.error.no_market");
            return;
        }

        AddMarketItemDialog dialog = new AddMarketItemDialog(player.locale(), template, itemStack);
        dialog.show(player);
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
}