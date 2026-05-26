package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.model.brigadier.BrigadierCommandManager;
import dev.lumas.shops.interfaces.SubCommand;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Register(Autowire.BRIGADIER)
@CommandMeta(name = "shops", permission = "shops.command", usage = "/<command> <subcommand>")
public class CommandManager extends BrigadierCommandManager<SubCommand> {

}
