package dev.lumas.shops.components.backing;

import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.components.data.PurchaseReceipt;
import dev.lumas.shops.interfaces.Codec;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;

import java.util.UUID;

public class PurchaseReceiptCodec extends Codec<PurchaseReceipt> {

    @Override
    public TypeToken<PurchaseReceipt> type() {
        return TypeToken.get(PurchaseReceipt.class);
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, PurchaseReceipt value) {
        out.value(value.purchaser() + "@" + value.marketItemKey().asString());
    }

    @Override
    @SneakyThrows
    @SuppressWarnings("PatternValidation")
    public PurchaseReceipt read(JsonReader in) {
        String s = in.nextString();
        int at = s.indexOf('@');
        io(at >= 0, "Invalid PurchaseReceipt: " + s);
        return new PurchaseReceipt(UUID.fromString(s.substring(0, at)), Key.key(s.substring(at + 1)));
    }
}