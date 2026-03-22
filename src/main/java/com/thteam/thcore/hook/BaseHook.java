package com.thteam.thcore.hook;

import com.thteam.thcore.THCore;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Base class for all soft-depend API integrations.
 * Each hook checks if its target plugin is installed before attempting to load.
 *
 * To create a new hook:
 *   1. Extend BaseHook
 *   2. Override getPluginName() to return the exact plugin name
 *   3. Override load() to wire the API — throw an Exception if something fails
 *   4. Register it in HookManager
 */
public abstract class BaseHook {

    protected final THCore plugin;
    private boolean enabled = false;

    protected BaseHook(THCore plugin) {
        this.plugin = plugin;
    }

    /**
     * The exact name of the plugin as it appears in its plugin.yml.
     */
    public abstract String getPluginName();

    /**
     * Wire the API here. Called only if the plugin is present and enabled.
     * Throw any Exception to signal failure — it will be caught and logged.
     */
    protected abstract void load() throws Exception;

    /**
     * Called by HookManager. Checks presence, then calls load().
     *
     * @return true if the hook loaded successfully
     */
    public final boolean tryLoad() {
        if (!isPluginPresent()) {
            plugin.getLogger().info("[Hook] " + getHookName() + " not found, skipping.");
            return false;
        }
        try {
            load();
            enabled = true;
            plugin.getLogger().info("[Hook] " + getHookName() + " hooked successfully.");
        } catch (Exception e) {
            enabled = false;
            plugin.getLogger().warning("[Hook] Failed to load " + getHookName() + ": " + e.getMessage());
        }
        return enabled;
    }

    /** Returns true if the hook was loaded successfully. */
    public boolean isEnabled() {
        return enabled;
    }

    /** Human-readable name used in log messages. Defaults to the class name. */
    public String getHookName() {
        return getClass().getSimpleName();
    }

    private boolean isPluginPresent() {
        Plugin p = Bukkit.getPluginManager().getPlugin(getPluginName());
        return p != null && p.isEnabled();
    }
}
