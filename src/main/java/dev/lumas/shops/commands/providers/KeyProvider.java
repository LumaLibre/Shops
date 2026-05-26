package dev.lumas.shops.commands.providers;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import dev.lumas.core.model.brigadier.ArgumentTypeProvider;
import dev.lumas.shops.config.ShopsConfig;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;
import net.kyori.adventure.key.Key;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class KeyProvider implements ArgumentTypeProvider {

    @Override
    public ArgumentType<?> provide() {
        return new ShopsKeyArgumentType();
    }

    public static final class ShopsKeyArgumentType implements CustomArgumentType<Key, Key> {

        private static final DynamicCommandExceptionType INVALID_KEY = new DynamicCommandExceptionType(input ->
                new LiteralMessage("Invalid key '" + input + "'"));
        private static final ArgumentType<Key> NATIVE = ArgumentTypes.key();
        private static final char WHITESPACE = ' ';

        @Override
        @SuppressWarnings("PatternValidation")
        public Key parse(StringReader reader) throws CommandSyntaxException {
            int start = reader.getCursor();
            // Read until whitespace or eoi
            while (reader.canRead() && reader.peek() != WHITESPACE) {
                reader.skip();
            }
            String input = reader.getString().substring(start, reader.getCursor());
            if (input.isEmpty()) {
                reader.setCursor(start);
                throw INVALID_KEY.createWithContext(reader, "");
            }

            try {
                if (!input.contains(":")) {
                    return Key.key(ShopsConfig.instance().defaultNamespace(), input);
                }
                return Key.key(input);
            } catch (Exception e) {
                reader.setCursor(start);
                throw INVALID_KEY.createWithContext(reader, input);
            }
        }

        @Override
        public ArgumentType<Key> getNativeType() {
            return NATIVE;
        }
    }
}