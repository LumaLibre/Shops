package dev.lumas.shops.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.model.brigadier.BrigadierCommandManager;
import dev.lumas.shops.util.Viewers;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(name = "shops", permission = "shops.command", usage = "/<command> <subcommand>")
public class CommandManager extends BrigadierCommandManager {

    @Override
    public void buildRootExecutor(LiteralArgumentBuilder<CommandSourceStack> root, Commands commands) {
        root.executes(ctx -> {
            Viewers.sendMessage(ctx.getSource().getSender(), Component.text("Specify a subcommand."));
            return Command.SINGLE_SUCCESS;
        });
    }
}
