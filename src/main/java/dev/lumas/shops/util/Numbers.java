package dev.lumas.shops.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

@UtilityClass
public final class Numbers {

    public static <T extends Number> T unbox(T value, T defaultTo) {
        return value == null ? defaultTo : value;
    }

    public static double parseDouble(@Nullable String string, double defaultTo) {
        try {
            if (string == null) return defaultTo;
            return Double.parseDouble(normalize(string));
        } catch (NumberFormatException e) {
            return defaultTo;
        }
    }

    public static int parseInt(@Nullable String string, int defaultTo) {
        try {
            if (string == null) return defaultTo;
            return Integer.parseInt(normalize(string));
        } catch (NumberFormatException e) {
            return defaultTo;
        }
    }

    private static String normalize(String string) {
        return string.replaceAll(",", "").trim();
    }
}
