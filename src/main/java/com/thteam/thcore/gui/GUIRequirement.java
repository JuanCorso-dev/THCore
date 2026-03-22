package com.thteam.thcore.gui;

import com.thteam.thcore.api.THCoreAPI;
import com.thteam.thcore.hook.impl.PlaceholderAPIHook;
import com.thteam.thcore.hook.impl.PlayerPointsHook;
import com.thteam.thcore.hook.impl.VaultHook;
import org.bukkit.entity.Player;

/**
 * Functional interface representing a condition evaluated for a Player.
 *
 * Use the static factory methods for the most common conditions.
 * Since this is a @FunctionalInterface, any lambda works:
 *   GUIRequirement custom = player -> player.getLevel() >= 10;
 *
 * Combine conditions with .and(), .or(), .negate():
 *   GUIRequirement.permission("vip").or(GUIRequirement.money(500))
 *   GUIRequirement.permission("ban").negate()
 */
@FunctionalInterface
public interface GUIRequirement {

    /** Returns true if the player meets this requirement. */
    boolean test(Player player);

    // ================================================================
    // Static factories
    // ================================================================

    /**
     * Passes if the player HAS the given permission node.
     */
    static GUIRequirement permission(String node) {
        return player -> player.hasPermission(node);
    }

    /**
     * Passes if the player does NOT have the given permission node.
     */
    static GUIRequirement noPermission(String node) {
        return player -> !player.hasPermission(node);
    }

    /**
     * Passes if the player has at least the given amount of money (Vault).
     * Always returns false if Vault is not installed.
     */
    static GUIRequirement money(double amount) {
        return player -> {
            VaultHook vault = THCoreAPI.getVault();
            return vault != null && vault.has(player, amount);
        };
    }

    /**
     * Passes if the player has at least the given amount of PlayerPoints.
     * Always returns false if PlayerPoints is not installed.
     */
    static GUIRequirement playerPoints(int amount) {
        return player -> {
            PlayerPointsHook pp = THCoreAPI.getPlayerPoints();
            return pp != null && pp.hasPoints(player.getUniqueId(), amount);
        };
    }

    /**
     * Compares a PlaceholderAPI placeholder value against a literal using a comparator.
     *
     * @param placeholder The PAPI placeholder, e.g. "%player_level%"
     * @param operator    One of: "==", "!=", ">", "<", ">=", "<="
     * @param value       The value to compare against, e.g. "10"
     *
     * Examples:
     *   GUIRequirement.placeholder("%player_level%", ">=", "10")
     *   GUIRequirement.placeholder("%vault_eco_balance%", ">", "500")
     *   GUIRequirement.placeholder("%player_name%", "==", "Steve")
     *
     * Always returns false if PlaceholderAPI is not installed.
     */
    static GUIRequirement placeholder(String placeholder, String operator, String value) {
        return player -> {
            PlaceholderAPIHook papi = THCoreAPI.getPAPI();
            if (papi == null) return false;

            String resolved = papi.parse(player, placeholder).trim();

            return switch (operator) {
                case "==" -> resolved.equalsIgnoreCase(value);
                case "!=" -> !resolved.equalsIgnoreCase(value);
                case ">"  -> parseDouble(resolved) >  parseDouble(value);
                case "<"  -> parseDouble(resolved) <  parseDouble(value);
                case ">=" -> parseDouble(resolved) >= parseDouble(value);
                case "<=" -> parseDouble(resolved) <= parseDouble(value);
                default   -> false;
            };
        };
    }

    /**
     * Always passes — useful as a default/no-op requirement.
     */
    static GUIRequirement always() {
        return player -> true;
    }

    /**
     * Never passes — useful for temporarily hiding an item.
     */
    static GUIRequirement never() {
        return player -> false;
    }

    // ================================================================
    // Logical combinators (default methods)
    // ================================================================

    /**
     * Returns a requirement that passes only if BOTH this AND the other pass.
     */
    default GUIRequirement and(GUIRequirement other) {
        return player -> this.test(player) && other.test(player);
    }

    /**
     * Returns a requirement that passes if THIS OR the other passes.
     */
    default GUIRequirement or(GUIRequirement other) {
        return player -> this.test(player) || other.test(player);
    }

    /**
     * Returns a requirement that passes if this requirement FAILS.
     */
    default GUIRequirement negate() {
        return player -> !this.test(player);
    }

    // ================================================================
    // Internal helper
    // ================================================================

    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
