package com.thteam.thcore.gui;

import com.thteam.thcore.THCore;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Global listener that protects player inventories when a FullGUI is open.
 *
 * If a player disconnects or dies while a FullGUI has replaced their inventory,
 * this listener ensures the original inventory is restored before items are dropped
 * or the session ends.
 *
 * Registered once in THCore.onEnable().
 */
public class PlayerInventoryListener implements Listener {

    private final InventoryBackupManager backupManager;

    public PlayerInventoryListener(THCore plugin) {
        this.backupManager = plugin.getBackupManager();
    }

    /**
     * Restores inventory when a player disconnects with a FullGUI open.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (backupManager.hasBackup(player.getUniqueId())) {
            backupManager.restore(player);
        }
    }

    /**
     * Restores inventory before death item drops so the player doesn't lose their items.
     * HIGHEST priority ensures we run before plugins that modify death drops.
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (backupManager.hasBackup(player.getUniqueId())) {
            // Clear any GUI items that Bukkit would drop (they are not the player's items)
            event.getDrops().clear();
            // Restore the real inventory — it will be handled by vanilla drop logic
            backupManager.restore(player);
        }
    }
}
