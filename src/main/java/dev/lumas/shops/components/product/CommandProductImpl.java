package dev.lumas.shops.components.product;

import dev.lumas.shops.constants.suppliers.Products;
import dev.lumas.shops.interfaces.Product;
import dev.lumas.shops.util.Scheduling;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public record CommandProductImpl(String command) implements Product {

    @Override
    public Products type() {
        return Products.COMMAND;
    }

    @Override
    public void give(Player player, int amount) {
        String finalCommand = command.replace("{player}", player.getName()).replace("{amount}", String.valueOf(amount));
        Scheduling.global(() -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        });
    }

    @Override
    public Object get() {
        return command;
    }
}
