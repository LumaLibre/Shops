package dev.lumas.shops.components.requirement;

import org.bukkit.entity.Player;

public interface PermissionRequirement {

    String permission();

    /**
     * Extra text to add under the cost of the item.
     * @return the extra text to add under the cost of the item
     */
    String extraText();

    default boolean hasPermission(Player player) {
        return player.hasPermission(permission());
    }
}
