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
        return 0;
    }

    @Override
    public boolean withdraw(Player player, Integer amount) {
        return false;
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
    public String price() {
        Component customName = amount.itemStack().getItemMeta().customName();
        String simpleItemName;

        if (customName != null) {
            simpleItemName = PlainTextComponentSerializer.plainText().serialize(customName);
        } else {
            simpleItemName = amount.itemStack().getType().name();
        }

        return amount.amount() + "x " + simpleItemName;
    }

    public record ItemStackAmount(ItemStack itemStack, int amount) {
    }
}
