package dev.lumas.shops.components.serial;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Accessor;
import dev.lumas.shops.interfaces.Serial;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor
@RequiredArgsConstructor
public class SerialKeyToIntMap<K> extends Serial<SerialKeyToIntMap<K>> implements Accessor<Map<K, Integer>> {

    private final Function<K, Key> toKey;
    private final Function<Key, K> fromKey;
    private Map<K, Integer> backing = new LinkedHashMap<>();

    public static <K extends Keyed> SerialKeyToIntMap<K> ofKeyed(Function<Key, K> resolver) {
        return new SerialKeyToIntMap<>(Keyed::getKey, resolver);
    }


    public static SerialKeyToIntMap<Key> ofRaw() {
        return new SerialKeyToIntMap<>(Function.identity(), Function.identity());
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, SerialKeyToIntMap<K> value) {
        out.beginObject();
        for (Map.Entry<K, Integer> entry : value.backing.entrySet()) {
            out.name(toKey.apply(entry.getKey()).asString());
            out.value(entry.getValue());
        }
        out.endObject();
    }

    @Override
    @SneakyThrows
    public SerialKeyToIntMap<K> read(JsonReader in) {
        Map<K, Integer> result = new LinkedHashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            NamespacedKey key = NamespacedKey.fromString(in.nextName());
            int value = in.nextInt();
            result.put(fromKey.apply(key), value);
        }
        in.endObject();
        return new SerialKeyToIntMap<>(toKey, fromKey, result);
    }

    @Override
    public Map<K, Integer> get() {
        return backing;
    }

    @Override
    public void set(Map<K, Integer> value) {
        this.backing = new LinkedHashMap<>(value);
    }
}