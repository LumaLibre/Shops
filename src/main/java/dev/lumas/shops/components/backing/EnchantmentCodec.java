package dev.lumas.shops.components.backing;

import com.google.common.base.Preconditions;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.interfaces.Serial;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;

import java.io.IOException;

public class EnchantmentCodec extends Serial<Enchantment> {

    @Override
    public void write(JsonWriter out, Enchantment value) throws IOException {
        out.value(value.getKey().asString());
    }

    @Override
    public Enchantment read(JsonReader in) throws IOException {
        NamespacedKey key = NamespacedKey.fromString(in.nextString());
        Preconditions.checkNotNull(key, "Invalid enchantment key");
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .getOrThrow(key);
    }
}