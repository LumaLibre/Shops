package dev.lumas.shops.components.serial;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.components.backing.PersistentDataTypes;
import dev.lumas.shops.interfaces.Accessor;
import dev.lumas.shops.interfaces.Serial;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.IOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

public class SerialPersistentDataView extends Serial<SerialPersistentDataView> implements Accessor<Map<Key, SerialPersistentDataView.TypedValue>> {

    public record TypedValue(PersistentDataType<?, ?> type, Object value) {}

    private Map<Key, TypedValue> backing = new LinkedHashMap<>();

    public SerialPersistentDataView() {}

    public SerialPersistentDataView(Map<Key, TypedValue> initial) {
        this.backing = new LinkedHashMap<>(initial);
    }

    /** Snapshot every supported key from a real container. */
    public static SerialPersistentDataView snapshot(PersistentDataContainer pdc) {
        Map<Key, TypedValue> out = new LinkedHashMap<>();
        for (NamespacedKey key : pdc.getKeys()) {
            PersistentDataTypes.Entry<?, ?> entry = PersistentDataTypes.detect(pdc, key);
            if (entry == null) continue; // unsupported type, skip
            Object value = pdc.get(key, entry.type());
            out.put(key, new TypedValue(entry.type(), value));
        }
        return new SerialPersistentDataView(out);
    }

    /** Apply this view's contents back to a real container. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void applyTo(PersistentDataContainer pdc) {
        for (Map.Entry<Key, TypedValue> e : backing.entrySet()) {
            NamespacedKey key = new NamespacedKey(e.getKey().namespace(), e.getKey().value());
            PersistentDataType type = e.getValue().type();
            pdc.set(key, type, e.getValue().value());
        }
    }

    @Override
    public void write(JsonWriter out, SerialPersistentDataView value) throws IOException {
        out.beginObject();
        for (Map.Entry<Key, TypedValue> entry : value.backing.entrySet()) {
            out.name(entry.getKey().asString());
            writeTyped(out, entry.getValue());
        }
        out.endObject();
    }

    @Override
    public SerialPersistentDataView read(JsonReader in) throws IOException {
        Map<Key, TypedValue> result = new LinkedHashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            Key key = Key.key(in.nextName());
            result.put(key, readTyped(in));
        }
        in.endObject();
        return new SerialPersistentDataView(result);
    }

    private static void writeTyped(JsonWriter out, TypedValue tv) throws IOException {
        String id = PersistentDataTypes.idOf(tv.type());
        if (id == null) throw new IOException("Unregistered PersistentDataType: " + tv.type());
        out.beginObject();
        out.name("type").value(id);
        out.name("value");
        writeValue(out, id, tv.value());
        out.endObject();
    }

    private static TypedValue readTyped(JsonReader in) throws IOException {
        in.beginObject();
        String id = null;
        Object value = null;
        boolean readValue = false;
        // type may appear before or after value; buffer if needed
        while (in.hasNext()) {
            String name = in.nextName();
            if (name.equals("type")) {
                id = in.nextString();
                if (readValue) break;
            } else if (name.equals("value")) {
                if (id == null) {
                    throw new IOException("'value' before 'type' not supported; write 'type' first");
                }
                value = readValue(in, id);
                readValue = true;
            } else {
                in.skipValue();
            }
        }
        in.endObject();
        if (id == null) throw new IOException("Missing 'type'");
        PersistentDataTypes.Entry<?, ?> entry = PersistentDataTypes.byId(id);
        if (entry == null) throw new IOException("Unknown type id: " + id);
        return new TypedValue(entry.type(), value);
    }

    private static void writeValue(JsonWriter out, String id, Object value) throws IOException {
        switch (id) {
            case "byte"  -> out.value(((Byte) value).intValue());
            case "short" -> out.value(((Short) value).intValue());
            case "int"   -> out.value((Integer) value);
            case "long"  -> out.value((Long) value);
            case "float" -> out.value((Float) value);
            case "double"-> out.value((Double) value);
            case "boolean" -> out.value((Boolean) value);
            case "string"  -> out.value((String) value);
            case "byte_array" -> out.value(Base64.getEncoder().encodeToString((byte[]) value));
            case "int_array" -> {
                out.beginArray();
                for (int i : (int[]) value) out.value(i);
                out.endArray();
            }
            case "long_array" -> {
                out.beginArray();
                for (long l : (long[]) value) out.value(l);
                out.endArray();
            }
            case "container" -> {
                // Recurse: snapshot nested container and write as a view
                SerialPersistentDataView nested = snapshot((PersistentDataContainer) value);
                new SerialPersistentDataView().write(out, nested);
            }
            default -> throw new IOException("No writer for type id: " + id);
        }
    }

    private static Object readValue(JsonReader in, String id) throws IOException {
        return switch (id) {
            case "byte"   -> (byte) in.nextInt();
            case "short"  -> (short) in.nextInt();
            case "int"    -> in.nextInt();
            case "long"   -> in.nextLong();
            case "float"  -> (float) in.nextDouble();
            case "double" -> in.nextDouble();
            case "boolean"-> in.nextBoolean();
            case "string" -> in.nextString();
            case "byte_array" -> Base64.getDecoder().decode(in.nextString());
            case "int_array" -> {
                java.util.List<Integer> tmp = new java.util.ArrayList<>();
                in.beginArray();
                while (in.hasNext()) tmp.add(in.nextInt());
                in.endArray();
                int[] arr = new int[tmp.size()];
                for (int i = 0; i < arr.length; i++) arr[i] = tmp.get(i);
                yield arr;
            }
            case "long_array" -> {
                java.util.List<Long> tmp = new java.util.ArrayList<>();
                in.beginArray();
                while (in.hasNext()) tmp.add(in.nextLong());
                in.endArray();
                long[] arr = new long[tmp.size()];
                for (int i = 0; i < arr.length; i++) arr[i] = tmp.get(i);
                yield arr;
            }
            case "container" -> {
                // Read a nested view; caller will need a PersistentDataAdapterContext to actually
                // reconstitute a PersistentDataContainer when applying. For now, store the view.
                yield new SerialPersistentDataView().read(in);
            }
            default -> throw new IOException("No reader for type id: " + id);
        };
    }

    @Override
    public Map<Key, TypedValue> get() {
        return backing;
    }

    @Override
    public void set(Map<Key, TypedValue> value) {
        this.backing = new LinkedHashMap<>(value);
    }
}