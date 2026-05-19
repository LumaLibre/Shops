package dev.lumas.shops.gson;

import com.google.gson.Gson;
import dev.lumas.core.manager.Reflect;
import dev.lumas.core.manager.Services;
import dev.lumas.core.model.Service;
import dev.lumas.shops.Shops;
import dev.lumas.shops.interfaces.Serial;

import java.util.Map;

public final class GsonHolder implements Service {

    private final Gson gson;

    public GsonHolder() {
        Map<Class<?>, Object> resolvedAdapters = Map.of();
        Reflect reflect = Reflect.from(Shops.class)
                .scan(Serial.class);
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    public static GsonHolder getInstance() {
        return (GsonHolder) Services.getTracked(GsonHolder.class);
    }
}
