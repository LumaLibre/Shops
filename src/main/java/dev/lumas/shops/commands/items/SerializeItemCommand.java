package dev.lumas.shops.commands.items;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.lumas.core.annotation.Argument;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.BrigadierExecutor;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.annotation.Suggests;
import dev.lumas.core.model.brigadier.ArgumentTypeProvider;
import dev.lumas.core.model.brigadier.BrigadierSubCommand;
import dev.lumas.shops.commands.CommandManager;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(
        name = "serialize",
        playerOnly = true,
        parent = CommandManager.class,
        permission = "shops.command.serialize",
        usage = "/<command> serialize [flags...]"
)
public class SerializeItemCommand implements BrigadierSubCommand {

    private static final List<String> FLAGS = List.of("console", "base64", "print");
    private static final SimpleCommandExceptionType NO_ITEM = new SimpleCommandExceptionType(new LiteralMessage("No item in hand"));

    @BrigadierExecutor
    public void run(CommandSourceStack src, @Argument(value = "flags", optional = true, provider = GreedyStringProvider.class) @Nullable String flagsArg) throws CommandSyntaxException {
        Set<String> flags = parseFlags(flagsArg);
        Player player = (Player) src.getSender();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.isEmpty()) {
            throw NO_ITEM.create();
        }

        String serialized = flags.contains("base64")
                ? Base64.getEncoder().encodeToString(item.serializeAsBytes())
                : Bukkit.getUnsafe().serializeItemAsJson(item).getAsJsonObject().toString();

        if (flags.contains("console")) {
            Viewers.sendMessage(Bukkit.getConsoleSender(), Component.text(serialized));
        }

        boolean print = flags.contains("print");
        Component message = MiniMessage.miniMessage()
                .deserialize(print ? serialized : "<b><dark_green>[Serialized Item]")
                .hoverEvent(Component.text(!print ? serialized : "Click to copy"))
                .clickEvent(ClickEvent.copyToClipboard(serialized));

        if (print) {
            player.sendMessage(message); // omit prefix
        } else {
            Viewers.sendMessage(player, message);
        }
    }

    @Suggests("flags")
    public CompletableFuture<Suggestions> suggestFlags(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String fullSoFar = builder.getInput().substring(0, builder.getStart());
        Set<String> alreadyTyped = parseFlags(fullSoFar);

        String partial = builder.getRemaining().toLowerCase();
        FLAGS.stream()
                .filter(f -> !alreadyTyped.contains(f))
                .filter(f -> f.startsWith(partial))
                .forEach(builder::suggest);

        return builder.buildFuture();
    }

    private static Set<String> parseFlags(@Nullable String raw) {
        if (raw == null || raw.isBlank()) {
            return Set.of();
        }
        Set<String> out = new HashSet<>();
        for (String token : raw.trim().split("\\s+")) {
            String lower = token.toLowerCase();
            if (FLAGS.contains(lower)) {
                out.add(lower);
            }
        }
        return out;
    }

    public static final class GreedyStringProvider implements ArgumentTypeProvider {
        @Override
        public ArgumentType<?> provide() {
            return StringArgumentType.greedyString();
        }
    }
}