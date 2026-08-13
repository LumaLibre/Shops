package dev.lumas.shops.components.backing.factories;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.interfaces.Product;

/**
 * Hands out the codecs for interfaces whose implementation is chosen by a {@code type} field:
 * products (a fixed enum) and currencies (an open registry).
 */
public class PolymorphicCodecFactory implements TypeAdapterFactory {

    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<?> raw = typeToken.getRawType();
        if (raw == Product.class) {
            return (TypeAdapter<T>) new ProductCodec(gson);
        }
        if (raw == Currency.class) {
            return (TypeAdapter<T>) new CurrencyCodec(gson);
        }
        return null;
    }
}
