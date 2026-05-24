package dev.lumas.shops.components;

import dev.lumas.shops.Shops;
import dev.lumas.shops.components.data.SlotEntry;
import dev.lumas.shops.components.data.SlotList;
import dev.lumas.shops.components.templates.MarketTemplate;
import dev.lumas.shops.constants.MarketSlot;
import dev.lumas.shops.interfaces.Meta;
import dev.lumas.shops.interfaces.ShopsInventory;
import dev.lumas.shops.manager.MarketManager;
import dev.lumas.shops.util.Viewers;
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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    private final Key key;
    private final Component title;
    private final int size;
    private final SlotList contentSlots;

    @Nullable
    private final MarketTemplate template;

    @Accessors(fluent = false)
    private final Inventory inventory;

    public MarketCreator(Key key, Component title, int size, SlotList contentSlots, @Nullable MarketTemplate template) {
        this.key = key;
        this.title = title;
        this.size = size;
        this.contentSlots = contentSlots;
        this.template = template;
        this.inventory = Bukkit.createInventory(this, size, title);

        for (Integer slot : contentSlots) {
            inventory.setItem(slot, CONTENT_SLOT_BLOCKER);
        }

        if (template != null) {
            for (SlotEntry entry : template.staticSlots()) {
                inventory.setItem(entry.slot(), entry.stack());
            }
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
        List<SlotEntry> staticSlots = getStaticSlots(locale);

        if (MarketManager.INSTANCE.exists(key)) {
            MarketManager.INSTANCE.edit(key, title, size, staticSlots, contentSlots);
            Viewers.sendMessage(player, "shops.messages.edit.success", key);
        } else {
            MarketManager.INSTANCE.create(key, title, size, staticSlots, contentSlots);
            Viewers.sendMessage(player, "shops.messages.create.success", key);
        }
    }

    protected List<SlotEntry> getStaticSlots(Locale locale) {
        // Snapshot old non-BORDER types by their original slot. Used to short-circuit
        // name-based detection when the player left a special slot untouched.
        Map<Integer, SlotEntry> previousByslot = new HashMap<>();
        if (template != null) {
            for (SlotEntry entry : template.staticSlots()) {
                if (entry.type() != MarketSlot.BORDER) {
                    previousByslot.put(entry.slot(), entry);
                }
            }
        }

        List<SlotEntry> staticSlots = new ArrayList<>();
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack == null || isContentSlot(itemStack)) continue;

            MarketSlot slot;
            SlotEntry previous = previousByslot.get(i);
            if (previous != null && previous.stack().isSimilar(itemStack)) {
                // Unchanged from before, keep the original type.
                slot = previous.type();
            } else {
                slot = determineSlotType(itemStack, locale);
            }
            staticSlots.add(new SlotEntry(i, slot, itemStack));
        }
        return staticSlots;
    }

    private MarketSlot determineSlotType(ItemStack itemStack, Locale locale) {
        ItemMeta meta = itemStack.getItemMeta();
        if (meta == null || !meta.hasCustomName()) {
            return MarketSlot.BORDER;
        }

        String name = PlainTextComponentSerializer.plainText().serialize(meta.customName());

        Pattern previous = pattern(GlobalTranslator.render(Component.translatable("shops.market.creator.previous"), locale));
        Pattern next = pattern(GlobalTranslator.render(Component.translatable("shops.market.creator.next"), locale));
        Pattern close = pattern(GlobalTranslator.render(Component.translatable("shops.market.creator.close"), locale));

        if (previous.matcher(name).find()) {
            return MarketSlot.PREVIOUS_PAGE;
        } else if (next.matcher(name).find()) {
            return MarketSlot.NEXT_PAGE;
        } else if (close.matcher(name).find()) {
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
