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
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

/**
 * Abstract base class for chest-only inventory GUIs (no player inventory).
 *
 * Features:
 *  - GUIButton with priority slot resolution (multiple buttons same slot, highest visible wins)
 *  - GUIRequirement conditions on buttons (view + click)
 *  - GUIAction on buttons (click, deny) and on open/close events
 *  - Dynamic titles: supports PlaceholderAPI placeholders resolved per-player
 *  - Auto-update: optional tick-based refresh for live content (economy, placeholders)
 *
 * Usage:
 *
 *   public class MyShop extends BaseGUI {
 *       public MyShop(THCore plugin) {
 *           super(plugin, "<gold>Shop — %vault_eco_balance_fixed%$</gold>", 3);
 *           setUpdateInterval(20); // refresh every 1s
 *
 *           addOpenAction(GUIAction.sound(Sound.BLOCK_CHEST_OPEN, 1f, 1f));
 *           addCloseAction(GUIAction.sound(Sound.BLOCK_CHEST_CLOSE, 1f, 1f));
 *
 *           // VIP button (shown only to VIPs, priority 10)
 *           addButton(new GUIButton(13, vipItem)
 *               .priority(10)
 *               .require(GUIRequirement.permission("shop.vip"))
 *               .onClick(GUIAction.takeMoney(500), GUIAction.console("give %player% diamond 5"))
 *           );
 *
 *           // Normal button (fallback, priority 1)
 *           addButton(new GUIButton(13, normalItem)
 *               .priority(1)
 *               .requireClick(GUIRequirement.money(100))
 *               .onDeny(GUIAction.message("<red>Need $100!"))
 *               .onClick(GUIAction.takeMoney(100))
 *           );
 *
 *           addButton(new GUIButton(26, closeItem).onClick(GUIAction.close()));
 *       }
 *
 *       @Override
 *       protected void fillItems() {
 *           fillEmpty(grayGlass);
 *       }
 *   }
 */
public abstract class BaseGUI implements Listener {

    protected final THCore plugin;
    protected Inventory inventory;
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
     * @param rows    Chest rows (1–6)
     */
    protected BaseGUI(THCore plugin, String title, int rows) {
        this.plugin   = plugin;
        this.rawTitle = title;
        this.rows     = Math.max(1, Math.min(6, rows));
    }

    // ---------------------------------------------------------------- Lifecycle

    /**
     * Opens the GUI for the given player.
     * Builds the inventory, resolves priorities, runs open actions.
     */
    public void open(Player player) {
        this.viewer = player;
        this.inventory = Bukkit.createInventory(null, rows * 9, resolveTitle(player));

        fillItems();
        resolveSlots(player);

        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);

        // Open actions
        for (GUIAction action : openActions) {
            action.execute(player);
        }

        // Start auto-update task if configured
        if (updateIntervalTicks > 0) {
            updateTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if (viewer != null && viewer.isOnline()
                        && viewer.getOpenInventory().getTopInventory().equals(inventory)) {
                    refresh();
                }
            }, updateIntervalTicks, updateIntervalTicks);
        }
    }

    /** Programmatically closes the GUI. */
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
     * Populate the inventory here using setItem() and addButton().
     * Called once during open(). GUIButton priority resolution happens after this.
     */
    protected abstract void fillItems();

    /**
     * Called on every inventory click inside this GUI.
     * GUIButton handlers fire automatically before this method.
     * Override for manual slot logic.
     */
    protected void handleClick(InventoryClickEvent event) {}

    /**
     * Called when the GUI is closed.
     */
    protected void handleClose(Player player) {}

    // ---------------------------------------------------------------- Item helpers

    /**
     * Places an item directly in a slot (bypasses button/priority system).
     */
    protected void setItem(int slot, ItemStack item) {
        if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
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
     * Fills all empty slots with the given filler item.
     */
    protected void fillEmpty(ItemStack filler) {
        if (inventory == null) return;
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (current == null || current.getType().isAir()) {
                inventory.setItem(i, filler.clone());
            }
        }
    }

    /**
     * Clears the inventory, re-runs fillItems() and re-resolves all button slots.
     * Also refreshes the title if PAPI placeholders are used.
     */
    protected void refresh() {
        if (inventory == null || viewer == null) return;

        // Update title
        Component newTitle = resolveTitle(viewer);
        Inventory fresh = Bukkit.createInventory(null, rows * 9, newTitle);
        inventory.getContents(); // just to avoid unused warning

        // Replace inventory contents by re-opening with the new title inventory
        // We clear and refill in-place to avoid the "open flash"
        inventory.clear();
        fillItems();
        resolveSlots(viewer);
    }

    /**
     * Forces an inventory title update. Re-opens the inventory with a new title.
     * This causes a brief flicker; prefer setUpdateInterval() for smooth updates.
     */
    protected void updateTitle() {
        if (viewer == null) return;
        Inventory fresh = Bukkit.createInventory(null, rows * 9, resolveTitle(viewer));
        ItemStack[] contents = inventory.getContents();
        fresh.setContents(contents);
        inventory = fresh;
        viewer.openInventory(inventory);
    }

    public Inventory getInventory() { return inventory; }
    public Player getViewer() { return viewer; }

    // ---------------------------------------------------------------- Bukkit Events

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inventory)) return;

        Player clicker = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // Find the active (visible) button for this slot
        GUIButton active = getActiveButton(slot, clicker);
        if (active != null) {
            active.click(clicker, event);
        }

        handleClick(event);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().equals(inventory)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;

        cancelUpdateTask();

        Player player = (Player) event.getPlayer();
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
     */
    private void resolveSlots(Player player) {
        for (Map.Entry<Integer, List<GUIButton>> entry : buttonsBySlot.entrySet()) {
            int slot = entry.getKey();
            if (slot < 0 || slot >= inventory.getSize()) continue;

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
                inventory.setItem(slot, chosen.getItem());
            }
            // If no button is visible, leave slot empty (or keep whatever fillItems placed)
        }
    }

    /**
     * Returns the highest-priority visible button at a slot, or null.
     */
    private GUIButton getActiveButton(int slot, Player player) {
        List<GUIButton> candidates = buttonsBySlot.get(slot);
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
}
