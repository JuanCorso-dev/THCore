package com.thteam.thcore.gui;

import com.thteam.thcore.THCore;
import com.thteam.thcore.api.THCoreAPI;
import com.thteam.thcore.hook.impl.PlaceholderAPIHook;
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
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

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
 * Features:
 *  - GUIButton with priority slot resolution (multiple buttons same slot, highest visible wins)
 *  - GUIRequirement conditions on buttons (view + click)
 *  - GUIAction on buttons (click, deny) and on open/close events
 *  - Dynamic titles: supports PlaceholderAPI placeholders resolved per-player
 *  - Auto-update: optional tick-based refresh for live content (economy, placeholders)
 *  - Full inventory backup/restore on open/close
 *
 * Usage:
 *
 *   public class MyFullMenu extends FullGUI {
 *       public MyFullMenu(THCore plugin) {
 *           super(plugin, "<gold>Full Menu — %vault_eco_balance_fixed%$</gold>", 6);
 *           setUpdateInterval(20); // refresh every 1s
 *
 *           addOpenAction(GUIAction.sound(Sound.BLOCK_CHEST_OPEN, 1f, 1f));
 *           addCloseAction(GUIAction.sound(Sound.BLOCK_CHEST_CLOSE, 1f, 1f));
 *
 *           addButton(new GUIButton(13, vipItem)
 *               .priority(10)
 *               .require(GUIRequirement.permission("menu.vip"))
 *               .onClick(GUIAction.console("give %player% diamond 1"))
 *           );
 *
 *           addButton(new GUIButton(13, normalItem)
 *               .priority(1)
 *               .requireClick(GUIRequirement.money(100))
 *               .onDeny(GUIAction.message("<red>Need $100!"))
 *               .onClick(GUIAction.takeMoney(100))
 *           );
 *
 *           addButton(new GUIButton(81, barrierItem).onClick(GUIAction.close()));
 *       }
 *
 *       @Override
 *       protected void fillItems() {
 *           fillEmpty(grayGlass);
 *       }
 *   }
 */
public abstract class FullGUI implements Listener {

    protected final THCore plugin;
    protected Inventory topInventory;
    protected Player viewer;

    // Raw (unparsed) title — PAPI resolved per-player on open/refresh
    private final String rawTitle;
    private final int rows;

    // All registered buttons, grouped by slot for priority resolution
    private final Map<Integer, List<GUIButton>> buttonsBySlot = new LinkedHashMap<>();

    // Open and close action lists
    private final List<GUIAction> openActions  = new ArrayList<>();
    private final List<GUIAction> closeActions = new ArrayList<>();

    // Auto-update: 0 = disabled
    private int updateIntervalTicks = 0;
    private BukkitTask updateTask = null;

    // ---------------------------------------------------------------- Constructor

    /**
     * @param plugin  THCore instance
     * @param title   Inventory title — MiniMessage, legacy &-codes, and PAPI placeholders
     * @param rows    Number of rows in the CHEST part (1–6). Player inv is always added.
     */
    protected FullGUI(THCore plugin, String title, int rows) {
        this.plugin   = plugin;
        this.rawTitle = title;
        this.rows     = Math.max(1, Math.min(6, rows));
    }

    // ---------------------------------------------------------------- Lifecycle

    /**
     * Opens the full GUI for the given player.
     * Backs up and clears the player's inventory, then calls fillItems().
     */
    public void open(Player player) {
        this.viewer = player;

        // Backup and clear the player's bottom inventory
        plugin.getBackupManager().backupAndClear(player);

        // Create the chest (top) inventory with resolved title
        topInventory = Bukkit.createInventory(null, rows * 9, resolveTitle(player));

        fillItems();
        resolveSlots(player);

        player.openInventory(topInventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Open actions
        for (GUIAction action : openActions) {
            action.execute(player);
        }

        // Start auto-update task if configured
        if (updateIntervalTicks > 0) {
            updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (viewer != null && viewer.isOnline()
                        && viewer.getOpenInventory().getTopInventory().equals(topInventory)) {
                    refresh();
                }
            }, updateIntervalTicks, updateIntervalTicks);
        }
    }

    /**
     * Programmatically closes the GUI.
     * Inventory restoration is handled by onInventoryClose().
     */
    public void close() {
        if (viewer != null) viewer.closeInventory();
    }

    // ---------------------------------------------------------------- Configuration (call in constructor)

    /**
     * Sets the auto-refresh interval in ticks (20 ticks = 1 second).
     * Set to 0 to disable (default). Call in the constructor.
     */
    protected void setUpdateInterval(int ticks) {
        this.updateIntervalTicks = Math.max(0, ticks);
    }

    /** Adds actions to execute when the GUI is opened. */
    protected void addOpenAction(GUIAction... actions) {
        openActions.addAll(Arrays.asList(actions));
    }

    /** Adds actions to execute when the GUI is closed. */
    protected void addCloseAction(GUIAction... actions) {
        closeActions.addAll(Arrays.asList(actions));
    }

    // ---------------------------------------------------------------- Abstract / Override

    /**
     * Populate the inventory with items here.
     * Use setItem(unifiedSlot, item) and addButton(GUIButton).
     * Called once during open(). GUIButton priority resolution happens after this.
     */
    protected abstract void fillItems();

    /**
     * Called when the player clicks any slot in this GUI.
     * The event is already cancelled. GUIButton handlers fire automatically before this.
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

    // ---------------------------------------------------------------- Item helpers

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
     * Registers a GUIButton.
     * Multiple buttons with the same slot are allowed — highest priority visible one wins.
     */
    protected void addButton(GUIButton button) {
        buttonsBySlot
            .computeIfAbsent(button.getSlot(), k -> new ArrayList<>())
            .add(button);
    }

    /**
     * Fills all empty slots in BOTH inventories with the given filler item.
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
     * Clears both inventories, re-runs fillItems(), and re-resolves all button slots.
     * Also refreshes the title if PAPI placeholders are used.
     */
    protected void refresh() {
        if (topInventory == null || viewer == null) return;

        topInventory.clear();
        viewer.getInventory().setStorageContents(new ItemStack[36]);

        fillItems();
        resolveSlots(viewer);
    }

    /**
     * Forces an inventory title update. Re-opens the inventory with a new title.
     * This causes a brief flicker; prefer setUpdateInterval() for smooth content updates.
     */
    protected void updateTitle() {
        if (viewer == null) return;
        Inventory fresh = Bukkit.createInventory(null, rows * 9, resolveTitle(viewer));
        ItemStack[] contents = topInventory.getContents();
        fresh.setContents(contents);
        topInventory = fresh;
        viewer.openInventory(topInventory);
    }

    public Inventory getTopInventory() { return topInventory; }
    public Player getViewer() { return viewer; }

    // ---------------------------------------------------------------- Bukkit Events

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(topInventory)) return;
        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;

        int unifiedSlot;

        if (event.getClickedInventory().equals(topInventory)) {
            unifiedSlot = event.getSlot();
        } else if (event.getClickedInventory().equals(event.getWhoClicked().getInventory())) {
            int playerSlot = event.getSlot();
            if (playerSlot <= 8) {
                unifiedSlot = playerSlot + 81; // hotbar (0-8) → 81-89
            } else {
                unifiedSlot = playerSlot + 45; // main rows (9-35) → 54-80
            }
        } else {
            return; // armor slots or other — ignore
        }

        Player clicker = (Player) event.getWhoClicked();

        // Find the active (visible) button for this slot
        GUIButton active = getActiveButton(unifiedSlot, clicker);
        if (active != null) {
            active.click(clicker, event);
        }

        handleClick(event, unifiedSlot);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(topInventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(topInventory)) return;

        cancelUpdateTask();

        Player player = (Player) event.getPlayer();

        // Restore the player's original inventory
        plugin.getBackupManager().restore(player);

        for (GUIAction action : closeActions) {
            action.execute(player);
        }

        handleClose(player);
        HandlerList.unregisterAll(this);
    }

    // ---------------------------------------------------------------- Internal helpers

    /**
     * For each slot that has registered buttons, places the item of the
     * highest-priority button whose viewRequirement passes for the viewer.
     * Works across both chest (0–53) and player inventory (54–89) slots.
     */
    private void resolveSlots(Player player) {
        for (Map.Entry<Integer, List<GUIButton>> entry : buttonsBySlot.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot > 89) continue;

            List<GUIButton> candidates = entry.getValue();
            candidates.sort(Comparator.comparingInt(GUIButton::getPriority).reversed());

            GUIButton chosen = null;
            for (GUIButton btn : candidates) {
                if (btn.isVisible(player)) {
                    chosen = btn;
                    break;
                }
            }

            if (chosen != null) {
                setItem(slot, chosen.getItem());
            }
        }
    }

    /**
     * Returns the highest-priority visible button at a unified slot, or null.
     */
    private GUIButton getActiveButton(int unifiedSlot, Player player) {
        List<GUIButton> candidates = buttonsBySlot.get(unifiedSlot);
        if (candidates == null) return null;
        return candidates.stream()
            .sorted(Comparator.comparingInt(GUIButton::getPriority).reversed())
            .filter(b -> b.isVisible(player))
            .findFirst()
            .orElse(null);
    }

    /** Resolves the raw title string with PAPI placeholders for a specific player. */
    private Component resolveTitle(Player player) {
        String parsed = rawTitle;
        PlaceholderAPIHook papi = THCoreAPI.getPAPI();
        if (papi != null) {
            parsed = papi.parse(player, parsed);
        }
        return MessageUtil.colorize(parsed);
    }

    private void cancelUpdateTask() {
        if (updateTask != null && !updateTask.isCancelled()) {
            updateTask.cancel();
            updateTask = null;
        }
    }

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
}
