package dev.lumas.shops.interfaces;

import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;

public abstract class Codec<T> extends TypeAdapter<T> {

    public abstract TypeToken<T> type();
}
