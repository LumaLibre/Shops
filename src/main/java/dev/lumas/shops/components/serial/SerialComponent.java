package dev.lumas.shops.components.serial;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Accessor;
import dev.lumas.shops.interfaces.Serial;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;

@AllArgsConstructor
public class SerialComponent extends Serial<SerialComponent> implements Accessor<Component> {

    private Component component;

    @Override
    @SneakyThrows
    public void write(JsonWriter out, SerialComponent value) {
        out.jsonValue(JSONComponentSerializer.json().serialize(value.get()));
    }

    @Override
    @SneakyThrows
    public SerialComponent read(JsonReader in) {
        return new SerialComponent(JSONComponentSerializer.json().deserialize(in.nextString()));
    }

    @Override
    public Component get() {
        return component;
    }

    @Override
    public void set(Component value) {
        this.component = value;
    }
}
