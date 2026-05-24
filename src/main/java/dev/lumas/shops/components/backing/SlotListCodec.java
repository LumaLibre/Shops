package dev.lumas.shops.components.backing;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.components.data.SlotList;
import dev.lumas.shops.interfaces.Codec;
import lombok.SneakyThrows;

import java.util.ArrayList;
import java.util.List;

public class SlotListCodec extends Codec<SlotList> {

    @Override
    public TypeToken<SlotList> type() {
        return TypeToken.get(SlotList.class);
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, SlotList value) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < value.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(value.get(i));
        }
        sb.append(']');
        out.jsonValue(sb.toString());
    }

    @Override
    @SneakyThrows
    public SlotList read(JsonReader in) {
        List<Integer> slots = new ArrayList<>();
        in.beginArray();
        while (in.hasNext()) slots.add(in.nextInt());
        in.endArray();
        return new SlotList(slots);
    }
}