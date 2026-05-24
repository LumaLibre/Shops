package dev.lumas.shops.components.backing;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.annotations.Singleton;
import dev.lumas.shops.components.MarketItem;
import dev.lumas.shops.components.data.Stock;
import dev.lumas.shops.manager.GsonHolder;
import dev.lumas.shops.interfaces.Codec;
import dev.lumas.shops.interfaces.Currency;
import dev.lumas.shops.interfaces.Product;
import lombok.SneakyThrows;
import net.kyori.adventure.key.Key;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.Map;

public class MarketItemMapCodec extends Codec<Map<Key, MarketItem>> {

    private static final TypeToken<Map<Key, MarketItem>> TYPE = new TypeToken<>() {};
    private static final TypeToken<MarketItem> ITEM_TYPE = TypeToken.get(MarketItem.class);

    @Override
    public TypeToken<Map<Key, MarketItem>> type() {
        return TYPE;
    }

    @Override
    @SneakyThrows
    public void write(JsonWriter out, Map<Key, MarketItem> value) {
        TypeAdapter<MarketItem> itemAdapter = GsonHolder.instance().get().getAdapter(ITEM_TYPE);

        out.beginObject();
        for (Map.Entry<Key, MarketItem> entry : value.entrySet()) {
            out.name(entry.getKey().asString());
            // Reflective adapter skips `key` because it's transient on MarketItem
            itemAdapter.write(out, entry.getValue());
        }
        out.endObject();
    }

    @Override
    @SneakyThrows
    public Map<Key, MarketItem> read(JsonReader in) {
        Gson gson = GsonHolder.instance().get();

        Map<Key, MarketItem> result = new LinkedHashMap<>();
        in.beginObject();
        while (in.hasNext()) {
            String keyString = in.nextName();
            Key itemKey = Key.key(keyString);
            JsonObject body = JsonParser.parseReader(in).getAsJsonObject();

            Stock stock = gson.fromJson(body.get("stock"), Stock.class);
            @SuppressWarnings("unchecked")
            Currency<? extends Number> currency = gson.fromJson(body.get("currency"), Currency.class);
            Product product = gson.fromJson(body.get("product"), Product.class);
            ItemStack stack = gson.fromJson(body.get("stack"), ItemStack.class);

            result.put(itemKey, new MarketItem(itemKey, stock, currency, product, stack));
        }
        in.endObject();
        return result;
    }
}