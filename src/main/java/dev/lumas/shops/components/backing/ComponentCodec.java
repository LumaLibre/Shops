package dev.lumas.shops.components.backing;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Codec;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentCodec extends Codec<Component> {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    @Override
    public TypeToken<Component> type() {
        return TypeToken.get(Component.class);
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, Component value) {
        // serialze to minimessage
        String serialized = miniMessage.serialize(value);
        out.value(serialized);
    }

    @Override
    @SneakyThrows
    public Component read(JsonReader in) {
        return miniMessage.deserialize(in.nextString());
    }
}
