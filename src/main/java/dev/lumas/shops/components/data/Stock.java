package dev.lumas.shops.components.data;

public record Stock(int player, int global) {

    private static final int THRESHOLD = 0;

    public boolean hasPlayerStock() {
        return player > THRESHOLD;
    }

    public boolean hasGlobalStock() {
        return global > THRESHOLD;
    }
}
