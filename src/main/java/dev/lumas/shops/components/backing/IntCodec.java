package dev.lumas.shops.components.backing;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Accessor;
import dev.lumas.shops.interfaces.Serial;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

@NoArgsConstructor
@AllArgsConstructor
public class IntCodec extends Serial<Integer> implements Accessor<Integer> {

    private Integer value;

    @Override
    @SneakyThrows
    public void write(JsonWriter out, Integer value) {
        out.value(value);
    }

    @Override
    @SneakyThrows
    public Integer read(JsonReader in) {
        return in.nextInt();
    }

    @Override
    public Integer get() {
        return value;
    }

    @Override
    public void set(Integer value) {
        this.value = value;
    }
}