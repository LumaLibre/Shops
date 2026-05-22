package dev.lumas.shops.components.data;

public record Stock(int player, int global) {

    public boolean hasPlayerStock() {
        return player > 0;
    }

    public boolean hasGlobalStock() {
        return global > 0;
    }
}
