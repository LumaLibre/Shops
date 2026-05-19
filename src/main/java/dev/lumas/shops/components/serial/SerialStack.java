package dev.lumas.shops.components.serial;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.components.SerialEnchantments;
import dev.lumas.shops.interfaces.BiTransformable;
import dev.lumas.shops.interfaces.Serial;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

@NullMarked
@AllArgsConstructor
public class SerialStack extends Serial<SerialStack> implements BiTransformable<ItemStack>, Keyed {

    private final Key key;
    private SerialComponent displayName;
    @Nullable
    private List<SerialComponent> lore;
    private SerialEnchantments enchantments;
    private SerialPersistentDataView persistentData;
    private List<ItemFlag> itemFlags;
    private Material material;
    private int amount;

    @MonotonicNonNull
    private ItemStack cache;

    @Override
    @SneakyThrows
    public void write(JsonWriter out, SerialStack value) {
        out.beginObject();

        out.name("key").value(value.key.asString());
        out.name("material").value(value.material.getKey().asString());
        out.name("amount").value(value.amount);

        out.name("displayName");
        value.displayName.write(out, value.displayName);

        out.name("lore");
        if (value.lore == null) {
            out.nullValue();
        } else {
            out.beginArray();
            for (SerialComponent c : value.lore) c.write(out, c);
            out.endArray();
        }

        out.name("enchantments");
        value.enchantments.write(out, value.enchantments);

        out.name("persistentData");
        value.persistentData.write(out, value.persistentData);

        out.name("itemFlags");
        out.beginArray();
        for (ItemFlag flag : value.itemFlags) out.value(flag.name());
        out.endArray();

        out.endObject();
    }

    @Override
    @SneakyThrows
    public SerialStack read(JsonReader in) {
        Key key = null;
        Material material = null;
        int amount = 1;
        SerialComponent displayName = null;
        List<SerialComponent> lore = null;
        SerialEnchantments enchantments = null;
        SerialPersistentDataView persistentData = null;
        List<ItemFlag> itemFlags = List.of();

        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "key" -> key = Key.key(in.nextString());
                case "material" -> material = Material.matchMaterial(in.nextString());
                case "amount" -> amount = in.nextInt();
                case "displayName" -> displayName = new SerialComponent(null).read(in);
                case "lore" -> {
                    if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                        in.nextNull();
                        lore = null;
                    } else {
                        List<SerialComponent> tmp = new ArrayList<>();
                        in.beginArray();
                        while (in.hasNext()) tmp.add(new SerialComponent(null).read(in));
                        in.endArray();
                        lore = tmp;
                    }
                }
                case "enchantments" -> enchantments = new SerialEnchantments().read(in);
                case "persistentData" -> persistentData = new SerialPersistentDataView().read(in);
                case "itemFlags" -> {
                    List<ItemFlag> tmp = new ArrayList<>();
                    in.beginArray();
                    while (in.hasNext()) tmp.add(ItemFlag.valueOf(in.nextString()));
                    in.endArray();
                    itemFlags = tmp;
                }
                default -> in.skipValue();
            }
        }
        in.endObject();

        Preconditions.checkNotNull(key, "key");
        Preconditions.checkNotNull(material, "material");
        Preconditions.checkNotNull(displayName, "displayName");
        Preconditions.checkNotNull(enchantments, "enchantments");
        return new SerialStack(key, displayName, lore, enchantments, persistentData, itemFlags, material, amount, null);
    }


    @Override
    public ItemStack transform() {
        if (cache != null) return cache;
        ItemStack item = ItemStack.of(material, amount);
        item.editMeta(meta -> {
            meta.displayName(displayName.get());
            if (lore != null) meta.lore(lore.stream().map(SerialComponent::get).toList());
            enchantments.get().forEach((enchantment, level) -> meta.addEnchant(enchantment, level, true));
            itemFlags.forEach(meta::addItemFlags);
            persistentData.applyTo(meta.getPersistentDataContainer());
        });

        this.cache = item;
        return item;
    }

    @Override
    public void accept(ItemStack value) {
        this.cache = value;
        this.material = value.getType();
        this.amount = value.getAmount();
        value.editMeta(meta -> {
            this.displayName = new SerialComponent(meta.displayName());

            List<Component> lore = meta.lore();
            if (lore == null) {
                this.lore = null;
            } else {
                this.lore = lore.stream().map(SerialComponent::new).toList();
            }

            this.enchantments = new SerialEnchantments(meta.getEnchants());
            this.itemFlags = List.copyOf(meta.getItemFlags());
            this.persistentData = SerialPersistentDataView.snapshot(meta.getPersistentDataContainer());
        });
    }

    @Override
    public Key key() {
        return key;
    }
}
