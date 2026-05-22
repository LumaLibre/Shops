package dev.lumas.shops.util;

import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;

@UtilityClass
public final class ClassUtil {

    public static String formatEnum(Enum<?> value) {
        return Arrays.stream(value.name().split("_"))
                .map(w -> w.charAt(0) + w.substring(1).toLowerCase(Locale.ROOT))
                .collect(Collectors.joining(" "));
    }
}
