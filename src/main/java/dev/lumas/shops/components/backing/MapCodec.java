package dev.lumas.shops.components.backing;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Accessor;
import dev.lumas.shops.interfaces.Serial;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RequiredArgsConstructor
public class MapCodec<K, V> extends Serial<MapCodec<K, V>> implements Accessor<Map<K, V>> {

    private final Serial<K> keySerial;
    private final Serial<V> valueSerial;
    private Map<K, V> backing = new LinkedHashMap<>();

    public MapCodec(Serial<K> keySerial, Serial<V> valueSerial, Map<K, V> initial) {
        this(keySerial, valueSerial);
        this.backing = new LinkedHashMap<>(initial);
    }

    @Override
    public void write(JsonWriter out, MapCodec<K, V> value) throws IOException {
        out.beginArray();
        for (Map.Entry<K, V> entry : value.backing.entrySet()) {
            out.beginArray();
            keySerial.write(out, entry.getKey());
            valueSerial.write(out, entry.getValue());
            out.endArray();
        }
        out.endArray();
    }

    @Override
    public MapCodec<K, V> read(JsonReader in) throws IOException {
        Map<K, V> result = new LinkedHashMap<>();
        in.beginArray();
        while (in.hasNext()) {
            in.beginArray();
            K k = keySerial.read(in);
            V v = valueSerial.read(in);
            result.put(k, v);
            in.endArray();
        }
        in.endArray();
        return new MapCodec<>(keySerial, valueSerial, result);
    }

    @Override
    public Map<K, V> get() {
        return backing;
    }

    @Override
    public void set(Map<K, V> value) {
        this.backing = new LinkedHashMap<>(value);
    }
}