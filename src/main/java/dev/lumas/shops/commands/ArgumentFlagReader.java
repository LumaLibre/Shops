package dev.lumas.shops.commands;

import dev.lumas.shops.components.data.Pair;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class for reading and accessing argument flags from command inputs.
 * Argument flags are different from arguments and are intended to be used when
 * a command has multiple options that can be toggled on or off without always
 * needing to be specified. Argument flags should start with a hyphen (-) followed by a
 * single character or word (e.g., -page for page or -t for time).
 */
@Getter
@Setter
public class ArgumentFlagReader {

    private static final String FLAG_PREFIX = "-";
    private static final String DEFAULT_FLAG_VALUE = "true";

    private final Map<String, String> flags;
    private final List<String> newArguments;
    private CastHandler extraCasts;

    public ArgumentFlagReader(String[] args) {
        Pair<Map<String, String>, List<String>> parsed = parseArgs(List.of(args));
        this.flags = parsed.first();
        this.newArguments = parsed.second();
    }

    public ArgumentFlagReader(String[] args, CastHandler extraCasts) {
        this(args);
        this.extraCasts = extraCasts;
    }


    @Nullable
    public String getFlagValue(String flagName) {
        return flags.get(flagName);
    }


    public boolean getFlagValueAsBoolean(String flagName) {
        return Boolean.parseBoolean(flags.getOrDefault(flagName, "false")); // Default to false if not present
    }

    @NotNull
    public <T> T getFlagValueAs(String flagName, T defaultValue, Class<T> clazz) throws ClassCastException {
        String value = flags.get(flagName);
        if (value == null) {
            return defaultValue;
        }

        if (clazz == String.class) {
            return clazz.cast(value);
        } else if (clazz == Integer.class) {
            return clazz.cast(Integer.parseInt(value));
        } else if (clazz == Boolean.class) {
            return clazz.cast(Boolean.parseBoolean(value));
        } else if (clazz == Double.class) {
            return clazz.cast(Double.parseDouble(value));
        } else if (clazz == Float.class) {
            return clazz.cast(Float.parseFloat(value));
        } else if (clazz == Long.class) {
            return clazz.cast(Long.parseLong(value));
        } else if (extraCasts != null) {
            T casted = extraCasts.handle(value, clazz);
            if (casted != null) {
                return casted;
            }
        }
        return defaultValue;
    }

    /**
     * Parses a list of arguments to separate flags and their values from regular arguments.
     * @param args The list of command arguments to parse.
     * @return A Couple containing a map of flags and their values, and a list of regular arguments with the flags removed.
     */
    private static Pair<Map<String, String>, List<String>> parseArgs(List<String> args) {
        Map<String, String> flags = new HashMap<>();
        List<String> newArguments = new ArrayList<>();
        List<String> argList = new ArrayList<>(args);
        for (int i = 0; i < argList.size(); i++) {
            String arg = argList.get(i);

            if (arg.startsWith(FLAG_PREFIX) && arg.length() > 1) {
                String flagName = arg.substring(1);
                String flagValue = DEFAULT_FLAG_VALUE;

                // If the next argument exists and is not another flag, treat it as the flag value
                if (i + 1 < argList.size() && !argList.get(i + 1).startsWith(FLAG_PREFIX)) {
                    flagValue = argList.get(i + 1);
                    i++; // Skip next arg since it’s consumed as a value
                }

                flags.put(flagName, flagValue);
            } else {
                newArguments.add(arg);
            }
        }
        return Pair.of(flags, newArguments);
    }

    public interface CastHandler {
        @Nullable
        <T> T handle(String value, Class<T> clazz);
    }
}
