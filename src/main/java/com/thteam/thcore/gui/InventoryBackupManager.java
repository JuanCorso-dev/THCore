package com.thteam.thcore.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages inventory backups for players who open a FullGUI.
 *
 * When a FullGUI opens it replaces the player's bottom inventory with GUI items.
 * This manager saves the original inventory so it can be restored when the GUI closes.
 *
 * THCore registers a PlayerInventoryListener to call restore() on disconnect/death,
 * preventing item loss if the player leaves while the GUI is open.
 */
public class InventoryBackupManager {

    // Stores one snapshot per player
    private final Map<UUID, InventorySnapshot> backups = new HashMap<>();

    // ------------------------------------------------ Public API

    /**
     * Saves the player's current inventory contents and clears it.
     * Does nothing if the player already has a backup (nested GUI protection).
     */
    public void backupAndClear(Player player) {
        UUID uuid = player.getUniqueId();
        if (backups.containsKey(uuid)) return; // already backed up, don't overwrite

        PlayerInventory inv = player.getInventory();

        ItemStack[] contents = copyArray(inv.getStorageContents()); // slots 0-35
        ItemStack[] armor    = copyArray(inv.getArmorContents());    // slots 36-39
        ItemStack   offhand  = inv.getItemInOffHand().clone();

        backups.put(uuid, new InventorySnapshot(contents, armor, offhand));

        inv.setStorageContents(new ItemStack[36]);
        inv.setArmorContents(new ItemStack[4]);
        inv.setItemInOffHand(null);
    }

    /**
     * Restores the player's inventory from backup and removes the stored snapshot.
     * Safe to call even if no backup exists.
     */
    public void restore(Player player) {
        InventorySnapshot snapshot = backups.remove(player.getUniqueId());
        if (snapshot == null) return;

        PlayerInventory inv = player.getInventory();
        inv.setStorageContents(snapshot.contents());
        inv.setArmorContents(snapshot.armor());
        inv.setItemInOffHand(snapshot.offhand() != null ? snapshot.offhand() : new ItemStack(org.bukkit.Material.AIR));
        player.updateInventory();
    }

    /**
     * Returns true if the player currently has a stored inventory backup.
     */
    public boolean hasBackup(UUID uuid) {
        return backups.containsKey(uuid);
    }

    /**
     * Restores all online players who have a backup.
     * Called during THCore.onDisable() to prevent item loss on server shutdown.
     */
    public void restoreAll() {
        for (UUID uuid : backups.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                restore(player);
            }
        }
        backups.clear();
    }

    // ------------------------------------------------ Internal helpers

    private ItemStack[] copyArray(ItemStack[] original) {
        ItemStack[] copy = new ItemStack[original.length];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i] != null ? original[i].clone() : null;
        }
        return copy;
    }

    // ------------------------------------------------ Snapshot record

    /**
     * Immutable snapshot of a player's inventory at a point in time.
     */
    private record InventorySnapshot(
        ItemStack[] contents,  // storage contents (slots 0-35)
        ItemStack[] armor,     // armor slots (0=boots, 1=leggings, 2=chestplate, 3=helmet)
        ItemStack   offhand    // offhand slot
    ) {}
}
