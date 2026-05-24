package dev.lumas.shops.components.data;

import lombok.experimental.Delegate;
import org.jspecify.annotations.NullMarked;

import java.util.Iterator;
import java.util.List;

@NullMarked
public record SlotList(@Delegate List<Integer> slots) implements Iterable<Integer> {
    public static SlotList of(List<Integer> slots) {
        return new SlotList(List.copyOf(slots));
    }

    @Override
    public Iterator<Integer> iterator() {
        return slots.iterator();
    }
}