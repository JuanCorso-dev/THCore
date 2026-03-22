package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlaceholderAPIHook extends BaseHook {

    private THCorePAPIExpansion expansion;

    public PlaceholderAPIHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "PlaceholderAPI";
    }

    @Override
    protected void load() {
        expansion = new THCorePAPIExpansion(plugin);
        expansion.register();
    }

    /** Must be called on plugin disable to prevent memory leaks. */
    public void unregister() {
        if (expansion != null) {
            expansion.unregister();
        }
    }

    /**
     * Parses PlaceholderAPI placeholders in a string for a specific player.
     * Example: parse(player, "Your balance: %vault_eco_balance%")
     */
    public String parse(Player player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    /**
     * Parses placeholders for an offline player.
     */
    public String parse(OfflinePlayer player, String text) {
        return PlaceholderAPI.setPlaceholders(player, text);
    }

    // ------------------------------------------------ Internal PAPI expansion

    static class THCorePAPIExpansion extends PlaceholderExpansion {

        private final THCore plugin;

        THCorePAPIExpansion(THCore plugin) {
            this.plugin = plugin;
        }

        @Override
        public @NotNull String getIdentifier() {
            return "thcore";
        }

        @Override
        public @NotNull String getAuthor() {
            return "THTeam";
        }

        @Override
        public @NotNull String getVersion() {
            return plugin.getDescription().getVersion();
        }

        @Override
        public boolean persist() {
            return true; // stays registered across PAPI reloads
        }

        @Override
        public @Nullable String onPlaceholderRequest(Player player, @NotNull String params) {
            if (player == null) return "";

            return switch (params.toLowerCase()) {
                case "version" -> plugin.getDescription().getVersion();
                case "hooks"   -> String.valueOf(plugin.getHookManager().getActiveHookCount());
                case "db_type" -> plugin.getDatabaseManager().getType();
                default        -> null; // return null = placeholder not found
            };
        }
    }
}
