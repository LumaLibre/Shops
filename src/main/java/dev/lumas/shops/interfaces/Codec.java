package dev.lumas.shops.interfaces;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import lombok.SneakyThrows;

import java.io.IOException;

public abstract class Codec<T> extends TypeAdapter<T> {

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
