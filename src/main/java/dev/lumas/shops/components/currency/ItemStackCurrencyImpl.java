package dev.lumas.shops.components.currency;

import dev.lumas.shops.constants.suppliers.Currencies;
import dev.lumas.shops.interfaces.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

// TODO: Implement
public record ItemStackCurrencyImpl(ItemStackAmount amount) implements Currency<Integer> {

    @Override
    public Integer getBalance(Player player) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public boolean withdraw(Player player) {
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public Currencies type() {
        return Currencies.ITEMSTACK;
    }

    @Override
    public Object get() {
        return amount;
    }

    @Override
    public String readablePrice() {
        Component customName = amount.itemStack().getItemMeta().customName();
        String simpleItemName;

        if (customName != null) {
            simpleItemName = PlainTextComponentSerializer.plainText().serialize(customName);
        } else {
            simpleItemName = amount.itemStack().getType().name();
        }

        return amount.amount() + "x " + simpleItemName;
    }

    @Override
    public Integer price() {
        return amount.amount();
    }

    public record ItemStackAmount(ItemStack itemStack, int amount) {
    }
}
