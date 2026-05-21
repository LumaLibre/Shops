package dev.lumas.shops.gson;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import dev.lumas.core.annotation.Autowire;
import dev.lumas.core.annotation.Register;
import dev.lumas.core.manager.Reflect;
import dev.lumas.core.manager.Services;
import dev.lumas.core.model.Service;
import dev.lumas.shops.Shops;
import dev.lumas.shops.annotations.Singleton;
import dev.lumas.shops.components.backing.factories.EnumTypeCodecFactory;
import dev.lumas.shops.interfaces.Accessor;
import dev.lumas.shops.interfaces.Codec;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Register(Autowire.SERVICE)
public final class GsonHolder implements Service, Accessor<Gson> {

    private final Gson gson;

    public GsonHolder() {
        Map<TypeToken<?>, Object> resolvedAdapters = new HashMap<>();
        Set<Class<?>> reflect = Reflect.from(Shops.class)
                .scan(Codec.class);
        for (Class<?> clazz : reflect) {
            try {
                // check if the class is abstract, and interface, or a singleton
                if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                Singleton singleton = clazz.getAnnotation(Singleton.class);
                Codec<?> codec;

                if (singleton != null) {
                    String fieldName = singleton.value();
                    codec = (Codec<?>) Preconditions.checkNotNull(clazz.getDeclaredField(fieldName).get(null), "Singleton instance field '%s' is null in class %s".formatted(fieldName, clazz.getName()));
                } else {
                    codec = (Codec<?>) clazz.getDeclaredConstructor().newInstance();
                }
                resolvedAdapters.put(codec.type(), codec);
            } catch (ReflectiveOperationException e) {
                e.printStackTrace();
            }
        }
        GsonBuilder builder = new GsonBuilder();

        for (Map.Entry<TypeToken<?>, Object> entry : resolvedAdapters.entrySet()) {
            builder.registerTypeAdapter(entry.getKey().getType(), entry.getValue());
        }

        builder.registerTypeAdapterFactory(new EnumTypeCodecFactory());

        this.gson = builder.setPrettyPrinting().create();
    }

    @Override
    public void register() {

    }

    @Override
    public void unregister() {

    }

    public static GsonHolder instance() {
        return (GsonHolder) Services.getTracked(GsonHolder.class);
    }

    @Override
    public Gson get() {
        return gson;
    }
}
