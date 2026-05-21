package dev.lumas.shops.commands;

import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.CommandMeta;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.model.command.AbstractCommandManager;
import dev.lumas.shops.Shops;
import dev.lumas.shops.interfaces.SubCommand;
import org.jspecify.annotations.NullMarked;

@Register(Autowire.COMMAND)
@CommandMeta(name = "shops")
@NullMarked
public class CommandManager extends AbstractCommandManager<Shops, SubCommand> {
    public CommandManager() {
        super(Shops.instance());
    }
}
