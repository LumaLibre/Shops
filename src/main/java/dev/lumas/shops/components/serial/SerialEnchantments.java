package dev.lumas.shops.components;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import dev.lumas.shops.components.serial.SerialKeyToIntMap;
import dev.lumas.shops.interfaces.Accessor;
import dev.lumas.shops.interfaces.Serial;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import lombok.NoArgsConstructor;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

@NoArgsConstructor
public class SerialEnchantments extends Serial<SerialEnchantments> implements Accessor<Map<Enchantment, Integer>> {

    private final SerialKeyToIntMap<Enchantment> backing = SerialKeyToIntMap.ofKeyed(
            key -> RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.ENCHANTMENT)
                    .getOrThrow(key)
    );

    public SerialEnchantments(Map<Enchantment, Integer> enchantments) {
        backing.set(enchantments);
    }

    @Override
    public void write(JsonWriter out, SerialEnchantments value) {
        backing.write(out, value.backing);
    }

    @Override
    public SerialEnchantments read(JsonReader in) {
        return new SerialEnchantments(backing.read(in).get());
    }

    @Override
    public Map<Enchantment, Integer> get() {
        return backing.get();
    }

    @Override
    public void set(Map<Enchantment, Integer> value) {
        backing.set(value);
    }
}