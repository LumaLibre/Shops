package dev.lumas.shops.components.data;

public record Pair<F, S>(F first, S second) {

    public F a() {
        return first;
    }

    public S b() {
        return second;
    }

    public static <T, Y> Pair<T, Y> of(T first, Y second) {
        return new Pair<>(first, second);
    }
}
