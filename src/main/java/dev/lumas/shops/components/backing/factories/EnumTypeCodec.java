package dev.lumas.shops.components.backing.factories;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Codec;
import dev.lumas.shops.interfaces.EnumType;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

public abstract class EnumTypeCodec<E extends Enum<E>, T extends EnumType<E>> extends Codec<T> {

    protected final Gson gson;

    protected EnumTypeCodec(Gson gson) {
        this.gson = gson;
    }

    protected abstract Class<E> enumClass();

    protected abstract Class<?> inputTypeOf(E type);

    protected abstract T construct(E type, @Nullable Object value);

    @Override
    public void write(JsonWriter out, T value) throws IOException {
        E type = value.type();
        out.beginObject();
        out.name("type").value(type.name());
        Object inner = value.get();
        if (inner != null) {
            out.name("value");
            adapterFor(type).write(out, inner);
        }
        out.endObject();
    }

    @Override
    public T read(JsonReader in) throws IOException {
        E type = null;
        Object value = null;
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "type" -> type = Enum.valueOf(enumClass(), in.nextString());
                case "value" -> {
                    if (type == null) {
                        io("'value' before 'type' not supported");
                    }
                    value = adapterFor(type).read(in);
                }
                default -> in.skipValue();
            }
        }
        in.endObject();
        if (type == null) {
            io("Missing type");
        }
        return construct(type, value); // value may be null
    }

    @SuppressWarnings("unchecked")
    private TypeAdapter<Object> adapterFor(E type) {
        return (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(inputTypeOf(type)));
    }
}