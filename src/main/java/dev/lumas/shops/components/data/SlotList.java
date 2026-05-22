package dev.lumas.shops.components.data;

import java.util.List;

public record SlotList(List<Integer> slots) {
    public static SlotList of(List<Integer> slots) {
        return new SlotList(List.copyOf(slots));
    }
}