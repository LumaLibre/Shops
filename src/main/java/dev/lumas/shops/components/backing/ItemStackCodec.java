package dev.lumas.shops.components.backing;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.annotations.Singleton;
import dev.lumas.shops.interfaces.Codec;
import lombok.SneakyThrows;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;

@Singleton
public class ItemStackCodec extends Codec<ItemStack> {

    public static final ItemStackCodec INSTANCE = new ItemStackCodec();

    @Override
    public TypeToken<ItemStack> type() {
        return TypeToken.get(ItemStack.class);
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, ItemStack value) {
        out.value(Base64.getEncoder().encodeToString(value.serializeAsBytes()));
    }

    @Override
    @SneakyThrows
    public ItemStack read(JsonReader in) {
        return ItemStack.deserializeBytes(Base64.getDecoder().decode(in.nextString()));
    }
}