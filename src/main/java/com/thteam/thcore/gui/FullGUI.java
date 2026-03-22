package com.thteam.thcore.gui;

import com.thteam.thcore.THCore;
import com.thteam.thcore.message.MessageUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for full-inventory GUIs (chest + player inventory).
 *
 * Uses a UNIFIED slot system (0–89):
 *
 *   Chest (top):       0 –  8  →  Row 0
 *                      9 – 17  →  Row 1
 *                     18 – 26  →  Row 2
 *                     27 – 35  →  Row 3
 *                     36 – 44  →  Row 4
 *                     45 – 53  →  Row 5
 *   Player inv (bot): 54 – 62  →  Main row 1  (PlayerInv 9–17)
 *                     63 – 71  →  Main row 2  (PlayerInv 18–26)
 *                     72 – 80  →  Main row 3  (PlayerInv 27–35)
 *                     81 – 89  →  Hotbar       (PlayerInv 0–8)
 *
 * The player's inventory is backed up on open and restored on close.
 * The backup system also handles disconnect and death automatically.
 *
 * Usage:
 *
 *   public class MyFullMenu extends FullGUI {
 *       public MyFullMenu(THCore plugin) {
 *           super(plugin, "<gold>Full Menu</gold>", 6); // 6-row chest
 *       }
 *
 *       @Override
 *       protected void fillItems() {
 *           setItem(4,  new ItemStack(Material.COMPASS));  // center of top
 *           setItem(81, new ItemStack(Material.BARRIER));  // hotbar left = close btn
 *
 *           addButton(new GUIButton(13, someItem, e -> doSomething()));
 *       }
 *
 *       @Override
 *       protected void handleClick(InventoryClickEvent event, int unifiedSlot) {
 *           if (unifiedSlot == 81) close();
 *       }
 *   }
 *
 *   new MyFullMenu(plugin).open(player);
 */
public abstract class FullGUI implements Listener {

    protected final THCore plugin;
    protected Inventory topInventory;
    protected Player viewer;

    private final Component title;
    private final int rows;

    // GUIButton handlers registered via addButton()
    private final Map<Integer, GUIButton> buttonMap = new HashMap<>();

    // ------------------------------------------------ Constructor

    /**
     * @param plugin  THCore instance (or your plugin's instance if it holds THCore)
     * @param title   Inventory title — supports MiniMessage and legacy &-codes
     * @param rows    Number of rows in the CHEST part (1–6). Player inv is always added.
     */
    protected FullGUI(THCore plugin, String title, int rows) {
        this.plugin = plugin;
        this.title = MessageUtil.colorize(title);
        this.rows = Math.max(1, Math.min(6, rows));
    }

    // ------------------------------------------------ Lifecycle

    /**
     * Opens the full GUI for the given player.
     * Backs up and clears the player's inventory, then calls fillItems().
     */
    public void open(Player player) {
        this.viewer = player;

        // Backup and clear the player's bottom inventory
        plugin.getBackupManager().backupAndClear(player);

        // Create and populate the chest (top) inventory
        topInventory = Bukkit.createInventory(null, rows * 9, title);
        fillItems();

        // Dispatch registered GUIButtons
        for (GUIButton button : buttonMap.values()) {
            applyButton(button);
        }

        player.openInventory(topInventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Programmatically closes the GUI.
     * Inventory restoration is handled by onInventoryClose().
     */
    public void close() {
        if (viewer != null) {
            viewer.closeInventory();
        }
    }

    // ------------------------------------------------ Abstract / Override

    /**
     * Populate the inventory with items here.
     * Use setItem(unifiedSlot, item) and addButton(GUIButton).
     * Called once during open(), before the player sees the GUI.
     */
    protected abstract void fillItems();

    /**
     * Called when the player clicks any slot in this GUI.
     * The event is already cancelled. The unified slot is pre-calculated.
     *
     * @param event       The original Bukkit click event
     * @param unifiedSlot The slot in unified coordinates (0–89)
     */
    protected void handleClick(InventoryClickEvent event, int unifiedSlot) {}

    /**
     * Called when the GUI is closed (player or programmatic).
     * At this point the inventory has already been restored.
     */
    protected void handleClose(Player player) {}

    // ------------------------------------------------ Item helpers

    /**
     * Places an item using the unified slot system (0–89).
     *
     * Slots  0–53: placed in the chest (top) inventory.
     * Slots 54–80: placed in player's main inventory rows (PlayerInv 9–35).
     * Slots 81–89: placed in player's hotbar (PlayerInv 0–8).
     */
    protected void setItem(int unifiedSlot, ItemStack item) {
        if (viewer == null) return;

        if (unifiedSlot >= 0 && unifiedSlot < topInventory.getSize()) {
            topInventory.setItem(unifiedSlot, item);
        } else {
            int playerIndex = toPlayerIndex(unifiedSlot);
            if (playerIndex >= 0) {
                viewer.getInventory().setItem(playerIndex, item);
            }
        }
    }

    /**
     * Registers a GUIButton and places its item.
     * Click handling is dispatched automatically before handleClick() is called.
     */
    protected void addButton(GUIButton button) {
        buttonMap.put(button.getSlot(), button);
        if (viewer != null) {
            applyButton(button);
        }
    }

    /**
     * Fills all empty slots in BOTH inventories with the given filler item.
     * Useful for decorating blank spots with gray glass panes.
     */
    protected void fillEmpty(ItemStack filler) {
        if (topInventory == null || viewer == null) return;

        // Fill top inventory
        for (int i = 0; i < topInventory.getSize(); i++) {
            ItemStack current = topInventory.getItem(i);
            if (current == null || current.getType().isAir()) {
                topInventory.setItem(i, filler.clone());
            }
        }

        // Fill player inventory slots (54-89 unified → indices 0-35)
        PlayerInventory playerInv = viewer.getInventory();
        for (int playerIndex = 0; playerIndex < 36; playerIndex++) {
            ItemStack current = playerInv.getItem(playerIndex);
            if (current == null || current.getType().isAir()) {
                playerInv.setItem(playerIndex, filler.clone());
            }
        }
    }

    /**
     * Clears both inventories and re-runs fillItems() + registered GUIButtons.
     * Useful for refreshing dynamic content.
     */
    protected void refresh() {
        if (topInventory == null || viewer == null) return;

        topInventory.clear();
        viewer.getInventory().setStorageContents(new ItemStack[36]);

        fillItems();
        for (GUIButton button : buttonMap.values()) {
            applyButton(button);
        }
    }

    // ------------------------------------------------ Getters

    public Inventory getTopInventory() {
        return topInventory;
    }

    public Player getViewer() {
        return viewer;
    }

    // ------------------------------------------------ Bukkit Events

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(topInventory)) return;
        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;

        int unifiedSlot;

        if (event.getClickedInventory().equals(topInventory)) {
            // Click inside the chest portion
            unifiedSlot = event.getSlot();
        } else if (event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
            // Click inside the player inventory portion
            int playerSlot = event.getSlot();
            if (playerSlot <= 8) {
                unifiedSlot = playerSlot + 81; // hotbar (0-8) → 81-89
            } else {
                unifiedSlot = playerSlot + 45; // main rows (9-35) → 54-80
            }
        } else {
            return; // armor slots or other — ignore
        }

        // Dispatch GUIButton handler first, then handleClick
        GUIButton button = buttonMap.get(unifiedSlot);
        if (button != null) {
            button.click(event);
        }

        handleClick(event, unifiedSlot);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        // Cancel all drags to prevent items being moved into/out of GUI slots
        if (event.getInventory().equals(topInventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(topInventory)) return;

        Player player = (Player) event.getPlayer();

        // Restore the player's original inventory
        plugin.getBackupManager().restore(player);

        handleClose(player);
        HandlerList.unregisterAll(this); // prevent memory leak
    }

    // ------------------------------------------------ Private helpers

    /**
     * Converts a unified slot (54–89) to a PlayerInventory index (0–35).
     * Returns -1 if the slot is out of range.
     */
    private int toPlayerIndex(int unifiedSlot) {
        if (unifiedSlot >= 54 && unifiedSlot <= 80) {
            return unifiedSlot - 45; // main rows → PlayerInv 9-35
        } else if (unifiedSlot >= 81 && unifiedSlot <= 89) {
            return unifiedSlot - 81; // hotbar → PlayerInv 0-8
        }
        return -1;
    }

    private void applyButton(GUIButton button) {
        setItem(button.getSlot(), button.getItem());
    }
}
