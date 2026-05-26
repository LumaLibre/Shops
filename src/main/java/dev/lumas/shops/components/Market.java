package dev.lumas.shops.components;

import dev.lumas.shops.components.dialog.ConfirmationDialog;
import dev.lumas.shops.components.templates.MarketState;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.interfaces.PaginatedInventory;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.Delegate;
import net.kyori.adventure.key.Key;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

import java.util.List;
import java.util.Locale;

@Getter
@NullMarked
@Accessors(fluent = true)
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class Market extends PaginatedInventory<MarketItem> {

    @Delegate
    private final MarketState state;

    private final Locale locale;

    public Market(MarketTemplate template, MarketState state, Locale locale) {
        super(template);
        this.state = state;
        this.locale = locale;
        this.render();
    }

    @Override
    public Key key() {
        return template.key();
    }

    @Override
    protected List<MarketItem> items() {
        return template.itemList();
    }

    @Override
    protected ItemStack renderItem(MarketItem item) {
        return item.display(state, locale);
    }

    @Override
    protected void onContentClick(MarketItem item, InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        prePurchase(item, player, event.getClick().isRightClick());
    }

    public void prePurchase(MarketItem marketItem, Player player, boolean showAmountSelector) {
        ConfirmationDialog dialog = new ConfirmationDialog(player, this, marketItem, showAmountSelector);
        dialog.show(player);
    }
}