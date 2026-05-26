package dev.lumas.shops.util;

import lombok.experimental.UtilityClass;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class CollectionUtil {

    @SafeVarargs
    public static <T> List<T> ofNonNulls(T... elements) {
        List<T> list = new ArrayList<>();
        for (T element : elements) {
            if (element != null) {
                list.add(element);
            }
        }
        return list;
    }

    public static <T> List<T> ofNonNulls(@Nullable List<T> elements) {
        List<T> list = new ArrayList<>();
        if (elements != null) {
            for (T element : elements) {
                if (element != null) {
                    list.add(element);
                }
            }
        }
        return list;
    }
}
