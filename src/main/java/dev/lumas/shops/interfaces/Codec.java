package dev.lumas.shops.interfaces;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;

import java.io.IOException;

/**
 * Base class for all codecs, an abstract class that extends {@link TypeAdapter}.
 * @param <T> The type of the object that this codec will encode or decode.
 */
public abstract class Codec<T> extends TypeAdapter<T> {

    /**
     * Gets the type of the object that this codec will encode or decode.
     * @return The type of the object.
     */
    public abstract TypeToken<T> type();

    @SneakyThrows
    protected final void io(boolean expression, String message) {
        if (!expression) throw new IOException(message);
    }

    @SneakyThrows
    protected final void io(String message) {
        throw new IOException(message);
    }
}
