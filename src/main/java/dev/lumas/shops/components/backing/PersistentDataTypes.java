package dev.lumas.shops.components.backing;

import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PersistentDataTypes {

    /** A supported type, paired with the string discriminator used on disk. */
    public record Entry<P, C>(String id, PersistentDataType<P, C> type) {}

    private static final Map<String, Entry<?, ?>> BY_ID = new LinkedHashMap<>();
    private static final Map<PersistentDataType<?, ?>, String> ID_BY_TYPE = new LinkedHashMap<>();

    private static <P, C> void register(String id, PersistentDataType<P, C> type) {
        Entry<P, C> entry = new Entry<>(id, type);
        BY_ID.put(id, entry);
        ID_BY_TYPE.put(type, id);
    }

    static {
        register("byte", PersistentDataType.BYTE);
        register("short", PersistentDataType.SHORT);
        register("int", PersistentDataType.INTEGER);
        register("long", PersistentDataType.LONG);
        register("float", PersistentDataType.FLOAT);
        register("double", PersistentDataType.DOUBLE);
        register("string", PersistentDataType.STRING);
        register("boolean", PersistentDataType.BOOLEAN);
        register("byte_array", PersistentDataType.BYTE_ARRAY);
        register("int_array", PersistentDataType.INTEGER_ARRAY);
        register("long_array", PersistentDataType.LONG_ARRAY);
        register("container", PersistentDataType.TAG_CONTAINER);
        // Lists and nested containers can be added similarly via PersistentDataType.LIST.*
    }

    public static Entry<?, ?> byId(String id) {
        return BY_ID.get(id);
    }

    public static String idOf(PersistentDataType<?, ?> type) {
        return ID_BY_TYPE.get(type);
    }

    /** Probe a container to find which registered type a key holds. */
    public static Entry<?, ?> detect(PersistentDataContainer pdc, org.bukkit.NamespacedKey key) {
        for (Entry<?, ?> entry : BY_ID.values()) {
            if (pdc.has(key, entry.type())) return entry;
        }
        return null;
    }

    private PersistentDataTypes() {}
}