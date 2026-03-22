package com.thteam.thcore.api;

import com.thteam.thcore.THCore;
import com.thteam.thcore.config.ConfigManager;
import com.thteam.thcore.cooldown.CooldownManager;
import com.thteam.thcore.database.DatabaseManager;
import com.thteam.thcore.gui.InventoryBackupManager;
import com.thteam.thcore.hook.HookManager;
import com.thteam.thcore.hook.impl.*;
import com.thteam.thcore.message.MessageManager;

/**
 * Static access point for all THCore systems.
 * Use this class in your dependent plugins to access the core.
 *
 * Example:
 *   VaultHook vault = THCoreAPI.getVault();
 *   if (vault != null) {
 *       vault.deposit(player, 100);
 *   }
 *
 *   THCoreAPI.getCooldownManager().setCooldown(player.getUniqueId(), "my_skill", 5000L);
 *   THCoreAPI.getDatabase().executeUpdate("INSERT INTO ...", args);
 */
public final class THCoreAPI {

    private static THCore plugin;

    private THCoreAPI() {}

    /** Called once by THCore during onEnable(). Do not call from external plugins. */
    public static void init(THCore core) {
        plugin = core;
    }

    private static void checkInit() {
        if (plugin == null) {
            throw new IllegalStateException(
                "THCoreAPI is not initialized. Is THCore enabled and loaded before your plugin?"
            );
        }
    }

    // ------------------------------------------------ Core managers

    public static HookManager getHookManager() {
        checkInit();
        return plugin.getHookManager();
    }

    public static DatabaseManager getDatabase() {
        checkInit();
        return plugin.getDatabaseManager();
    }

    public static CooldownManager getCooldownManager() {
        checkInit();
        return plugin.getCooldownManager();
    }

    public static MessageManager getMessageManager() {
        checkInit();
        return plugin.getMessageManager();
    }

    public static ConfigManager getConfigManager() {
        checkInit();
        return plugin.getConfigManager();
    }

    public static InventoryBackupManager getBackupManager() {
        checkInit();
        return plugin.getBackupManager();
    }

    // ------------------------------------------------ Hook shortcuts
    // Each returns null if the corresponding plugin is not installed on the server.

    /** @return VaultHook or null if Vault is not installed */
    public static VaultHook getVault() {
        checkInit();
        return plugin.getHookManager().getHook(VaultHook.class);
    }

    /** @return PlaceholderAPIHook or null if PAPI is not installed */
    public static PlaceholderAPIHook getPAPI() {
        checkInit();
        return plugin.getHookManager().getHook(PlaceholderAPIHook.class);
    }

    /** @return WorldGuardHook or null if WorldGuard is not installed */
    public static WorldGuardHook getWorldGuard() {
        checkInit();
        return plugin.getHookManager().getHook(WorldGuardHook.class);
    }

    /** @return NexoHook or null if Nexo is not installed */
    public static NexoHook getNexo() {
        checkInit();
        return plugin.getHookManager().getHook(NexoHook.class);
    }

    /** @return OraxenHook or null if Oraxen is not installed */
    public static OraxenHook getOraxen() {
        checkInit();
        return plugin.getHookManager().getHook(OraxenHook.class);
    }

    /** @return ItemsAdderHook or null if ItemsAdder is not installed */
    public static ItemsAdderHook getItemsAdder() {
        checkInit();
        return plugin.getHookManager().getHook(ItemsAdderHook.class);
    }

    /** @return MythicMobsHook or null if MythicMobs is not installed */
    public static MythicMobsHook getMythicMobs() {
        checkInit();
        return plugin.getHookManager().getHook(MythicMobsHook.class);
    }

    /** @return ExcellentEconomyHook or null if CoinsEngine is not installed */
    public static ExcellentEconomyHook getExcellentEconomy() {
        checkInit();
        return plugin.getHookManager().getHook(ExcellentEconomyHook.class);
    }

    /** @return PlayerPointsHook or null if PlayerPoints is not installed */
    public static PlayerPointsHook getPlayerPoints() {
        checkInit();
        return plugin.getHookManager().getHook(PlayerPointsHook.class);
    }

    // ------------------------------------------------ Plugin instance (internal use)

    /** Returns the THCore plugin instance. Prefer the typed getters above. */
    public static THCore getPlugin() {
        checkInit();
        return plugin;
    }
}
