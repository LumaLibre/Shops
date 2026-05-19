package dev.lumas.shops.components.backing;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Codec;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;

public class KeyCodec extends Codec<Key> {
    @Override
    public TypeToken<Key> type() {
        return TypeToken.get(Key.class);
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, Key value) {
        out.value(value.asString());
    }

    @Override
    @SneakyThrows
    public Key read(JsonReader in) {
        return Key.key(in.nextString());
    }
}
