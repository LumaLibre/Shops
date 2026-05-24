package dev.lumas.shops.components.backing;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.internal.Streams;
import com.google.gson.internal.bind.JsonTreeWriter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Codec;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.util.Base64;

public class ItemStackCodec extends Codec<ItemStack> {

    @Override
    public TypeToken<ItemStack> type() {
        return TypeToken.get(ItemStack.class);
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, ItemStack value) {
        JsonObject obj = Bukkit.getUnsafe().serializeItemAsJson(value);

        if (out instanceof JsonTreeWriter) {
            // Tree writer doesn't support jsonValue emit as a tree.
            Streams.write(obj, out);
        } else {
            // Streaming writer compact form, ignore pretty-print indentation.
            out.jsonValue(obj.toString());
        }
    }

    @Override
    @SneakyThrows
    public ItemStack read(JsonReader in) {
        JsonToken token = in.peek();
        return switch (token) {
            case STRING -> ItemStack.deserializeBytes(Base64.getDecoder().decode(in.nextString()));
            case BEGIN_OBJECT -> {
                JsonObject obj = JsonParser.parseReader(in).getAsJsonObject();
                yield Bukkit.getUnsafe().deserializeItemFromJson(obj);
            }
            default -> throw new IllegalStateException("Expected STRING or BEGIN_OBJECT for ItemStack, got " + token);
        };
    }
}