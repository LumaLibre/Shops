package dev.lumas.shops.components;

import dev.lumas.shops.Shops;
import dev.lumas.shops.components.data.SlotEntry;
import dev.lumas.shops.components.data.SlotList;
import dev.lumas.shops.constants.MarketSlot;
import dev.lumas.shops.interfaces.Meta;
import dev.lumas.shops.interfaces.ShopsInventory;
import lombok.Getter;
import lombok.experimental.Accessors;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Getter
@NullMarked
@Accessors(fluent = true)
public class MarketCreator implements ShopsInventory {

    private static final NamespacedKey CONTENT_SLOT_KEY = new NamespacedKey(Shops.instance(), "content_slot");
    private static final ItemStack CONTENT_SLOT_BLOCKER = Meta.edit(ItemStack.of(Material.BARRIER), meta -> {
        meta.getPersistentDataContainer().set(CONTENT_SLOT_KEY, PersistentDataType.BOOLEAN, true);
        meta.setHideTooltip(true);
    });

    private final Key newMarketKey;
    private final Component newMarketTitle;
    private final int newMarketSize;
    private final SlotList newMarketContentSlots;

    @Accessors(fluent = false)
    private final Inventory inventory;

    public MarketCreator(Key newMarketKey, Component newMarketTitle, int newMarketSize, SlotList newMarketContentSlots) {
        this.newMarketKey = newMarketKey;
        this.newMarketTitle = newMarketTitle;
        this.newMarketSize = newMarketSize;
        this.newMarketContentSlots = newMarketContentSlots;
        this.inventory = Bukkit.createInventory(this, newMarketSize, newMarketTitle);

        for (Integer slot : newMarketContentSlots.slots()) {
            inventory.setItem(slot, CONTENT_SLOT_BLOCKER);
        }
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        if (isContentSlot(event.getCurrentItem())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void handleClose(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();
        Locale locale = player.locale();
        List<SlotEntry> staticSlots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack != null && !isContentSlot(itemStack)) {
                MarketSlot slot = determineSlotType(itemStack, locale);
                staticSlots.add(new SlotEntry(i, slot, itemStack));
            }
        }
        MarketManager.INSTANCE.create(newMarketKey, newMarketTitle, newMarketSize, staticSlots, newMarketContentSlots);
        player.sendMessage(Component.translatable("shops.create.success", Component.text(newMarketKey.asString())));
    }

    private MarketSlot determineSlotType(ItemStack itemStack, Locale locale) {

        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasCustomName()) {
            return MarketSlot.BORDER;
        }

        String name = PlainTextComponentSerializer.plainText().serialize(meta.customName());

        Pattern previous = pattern(GlobalTranslator.render(Component.translatable("shops.market.creator.previous"), locale));
        Pattern next = pattern(GlobalTranslator.render(Component.translatable("shops.market.creator.next"), locale));
        Pattern back = pattern(GlobalTranslator.render(Component.translatable("shops.market.creator.close"), locale));

        if (previous.matcher(name).matches()) {
            return MarketSlot.PREVIOUS_PAGE;
        } else if (next.matcher(name).matches()) {
            return MarketSlot.NEXT_PAGE;
        } else if (back.matcher(name).matches()) {
            return MarketSlot.CLOSE;
        } else {
            return MarketSlot.BORDER;
        }
    }

    private Pattern pattern(Component component) {
        return Pattern.compile(PlainTextComponentSerializer.plainText().serialize(component));
    }

    private boolean isContentSlot(@Nullable ItemStack itemStack) {
        return itemStack != null && itemStack.getPersistentDataContainer().has(CONTENT_SLOT_KEY);
    }
}
