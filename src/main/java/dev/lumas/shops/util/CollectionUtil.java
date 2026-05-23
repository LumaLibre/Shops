package dev.lumas.shops.util;

import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public final class CollectionUtil {

    @SafeVarargs
    public static <T> List<T> ofNonNull(T... elements) {
        List<T> list = new ArrayList<>();
        for (T element : elements) {
            if (element != null) {
                list.add(element);
            }
        }
        return list;
    }
}
