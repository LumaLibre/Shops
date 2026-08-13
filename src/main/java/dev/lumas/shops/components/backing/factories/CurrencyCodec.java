package dev.lumas.shops.components.backing.factories;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.core.annotation.ReflectIgnore;
import dev.lumas.shops.api.currency.CurrencyRegistry;
import dev.lumas.shops.api.currency.CurrencyType;
import dev.lumas.shops.interfaces.Codec;
import dev.lumas.shops.interfaces.Currency;
import org.jspecify.annotations.Nullable;

import java.io.IOException;

@ReflectIgnore
public class CurrencyCodec extends Codec<Currency<?>> {

    private final Gson gson;

    public CurrencyCodec(Gson gson) {
        this.gson = gson;
    }

    @Override
    public TypeToken<Currency<?>> type() {
        return new TypeToken<>() {};
    }

    @Override
    public void write(JsonWriter out, Currency<?> value) throws IOException {
        CurrencyType<?> type = value.type();
        out.beginObject();
        out.name("type").value(type.key().asString());
        Object amount = value.get();
        if (amount != null) {
            out.name("value");
            adapterFor(type).write(out, amount);
        }
        out.endObject();
    }

    @Override
    public Currency<?> read(JsonReader in) throws IOException {
        CurrencyType<?> type = null;
        Object amount = null;
        in.beginObject();
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "type" -> type = CurrencyRegistry.INSTANCE.require(in.nextString());
                case "value" -> {
                    if (type == null) {
                        io("'value' before 'type' not supported");
                    }
                    amount = adapterFor(type).read(in);
                }
                default -> in.skipValue();
            }
        }
        in.endObject();
        if (type == null) {
            throw new IOException("Missing currency type");
        }
        return create(type, amount);
    }

    @SuppressWarnings("unchecked")
    private static Currency<?> create(CurrencyType<?> type, @Nullable Object amount) {
        return ((CurrencyType<Object>) type).create(amount == null ? null : type.amountType().cast(amount));
    }

    @SuppressWarnings("unchecked")
    private TypeAdapter<Object> adapterFor(CurrencyType<?> type) {
        return (TypeAdapter<Object>) gson.getAdapter(TypeToken.get(type.amountType()));
    }
}
