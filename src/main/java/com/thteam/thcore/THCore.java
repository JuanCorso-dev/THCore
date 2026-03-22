package com.thteam.thcore;

import com.thteam.thcore.api.THCoreAPI;
import com.thteam.thcore.command.THCoreCommand;
import com.thteam.thcore.config.ConfigManager;
import com.thteam.thcore.cooldown.CooldownManager;
import com.thteam.thcore.database.DatabaseManager;
import com.thteam.thcore.gui.InventoryBackupManager;
import com.thteam.thcore.gui.PlayerInventoryListener;
import com.thteam.thcore.hook.HookManager;
import com.thteam.thcore.message.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * THCore — Core library plugin for Paper 1.21.1
 *
 * Initialization order (MUST be respected):
 *   1. ConfigManager         — everything else reads from config
 *   2. MessageManager        — used by all systems for output
 *   3. DatabaseManager       — reads DB type from config
 *   4. CooldownManager       — pure in-memory, no dependencies
 *   5. InventoryBackupManager — needed by FullGUI and PlayerInventoryListener
 *   6. HookManager           — needs Bukkit's PluginManager (all plugins already loaded)
 *   7. THCoreAPI             — static facade, initialized last
 *   8. Commands + Listeners  — registered after everything is wired
 */
public final class THCore extends JavaPlugin {

    private static THCore instance;

    private ConfigManager configManager;
    private MessageManager messageManager;
    private DatabaseManager databaseManager;
    private CooldownManager cooldownManager;
    private InventoryBackupManager backupManager;
    private HookManager hookManager;

    @Override
    public void onEnable() {
        instance = this;

        // 1. Config
        configManager = new ConfigManager(this);
        configManager.load();

        // 2. Messages
        messageManager = new MessageManager(this);

        // 3. Database
        databaseManager = DatabaseManager.create(this, configManager);
        databaseManager.connect();

        // 4. Cooldowns
        cooldownManager = new CooldownManager();

        // 5. Inventory backup (needed by FullGUI)
        backupManager = new InventoryBackupManager();
        getServer().getPluginManager().registerEvents(new PlayerInventoryListener(this), this);

        // 6. Hooks (soft-depend integrations)
        hookManager = new HookManager(this);
        hookManager.loadAll();

        // 7. Static API facade
        THCoreAPI.init(this);

        // 8. Built-in /thcore command
        new THCoreCommand(this).register(this, "thcore");

        getLogger().info("THCore v" + getDescription().getVersion() + " enabled. "
            + "Hooks: " + hookManager.getActiveHookCount() + "/" + hookManager.getAll().size()
            + " | DB: " + databaseManager.getType());
    }

    @Override
    public void onDisable() {
        if (backupManager != null) {
            backupManager.restoreAll(); // restore inventories before shutdown
        }
        if (hookManager != null) {
            hookManager.unloadAll();
        }
        if (databaseManager != null) {
            databaseManager.disconnect();
        }
        instance = null;
        getLogger().info("THCore disabled.");
    }

    // ------------------------------------------------ Static accessor

    /** Returns the THCore instance. Prefer THCoreAPI for external plugins. */
    public static THCore getInstance() {
        return instance;
    }

    // ------------------------------------------------ Getters

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CooldownManager getCooldownManager() {
        return cooldownManager;
    }

    public HookManager getHookManager() {
        return hookManager;
    }

    public InventoryBackupManager getBackupManager() {
        return backupManager;
    }
}
