package dev.lumas.shops.components.requirement;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record CustomRequirement(String permission, String extraText) implements PermissionRequirement {
}
