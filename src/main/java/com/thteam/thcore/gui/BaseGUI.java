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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Abstract base class for all inventory GUIs.
 *
 * Each instance represents ONE open GUI for ONE player.
 * It registers itself as a Bukkit listener on open() and
 * unregisters on close to prevent memory leaks.
 *
 * Usage:
 *
 *   public class MyShopGUI extends BaseGUI {
 *       public MyShopGUI(THCore plugin) {
 *           super(plugin, "<gold>My Shop</gold>", 3); // 3 rows
 *       }
 *
 *       @Override
 *       protected void fillItems() {
 *           setItem(13, new ItemStack(Material.DIAMOND));
 *       }
 *
 *       @Override
 *       protected void handleClick(InventoryClickEvent event) {
 *           if (event.getSlot() == 13) {
 *               event.getWhoClicked().sendMessage("You clicked the diamond!");
 *           }
 *       }
 *   }
 *
 *   // Open it:
 *   new MyShopGUI(plugin).open(player);
 */
public abstract class BaseGUI implements Listener {

    protected final THCore plugin;
    protected Inventory inventory;
    protected Player viewer;

    private final Component title;
    private final int rows;

    /**
     * @param plugin  The THCore instance (or your own plugin instance)
     * @param title   Inventory title — supports MiniMessage and legacy &-codes
     * @param rows    Number of rows (1–6)
     */
    protected BaseGUI(THCore plugin, String title, int rows) {
        this.plugin = plugin;
        this.title = MessageUtil.colorize(title);
        this.rows = Math.max(1, Math.min(6, rows));
    }

    // ------------------------------------------------ Lifecycle

    /**
     * Opens the GUI for the given player.
     * Creates the inventory, calls fillItems(), and registers this as a listener.
     */
    public void open(Player player) {
        this.viewer = player;
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        fillItems();
        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Programmatically closes the GUI and triggers handleClose().
     */
    public void close() {
        if (viewer != null) {
            viewer.closeInventory();
        }
    }

    // ------------------------------------------------ Abstract / Override

    /**
     * Populate the inventory with items here.
     * Called once during open(), before the player sees the inventory.
     */
    protected abstract void fillItems();

    /**
     * Called when the player clicks inside this GUI's inventory.
     * The event is already cancelled (items can't be taken). Override to add logic.
     */
    protected void handleClick(InventoryClickEvent event) {}

    /**
     * Called when the GUI is closed (by player or programmatically).
     * Override to add cleanup logic (e.g. save data).
     */
    protected void handleClose(Player player) {}

    // ------------------------------------------------ Helpers

    /**
     * Places an item in the given slot. Safe to call during fillItems().
     */
    protected void setItem(int slot, ItemStack item) {
        if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
    }

    /**
     * Fills every slot that currently has no item with the given filler.
     * Useful for decorating empty slots with gray glass panes.
     */
    protected void fillEmpty(ItemStack filler) {
        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack current = inventory.getItem(i);
            if (current == null || current.getType().isAir()) {
                inventory.setItem(i, filler);
            }
        }
    }

    /** Clears all items and re-runs fillItems(). Useful for paginated GUIs. */
    protected void refresh() {
        inventory.clear();
        fillItems();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getViewer() {
        return viewer;
    }

    // ------------------------------------------------ Bukkit Events

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        event.setCancelled(true); // prevent item theft by default

        // Only process clicks inside our GUI (not the player's bottom inventory)
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inventory)) return;

        handleClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        handleClose((Player) event.getPlayer());
        HandlerList.unregisterAll(this); // prevents memory leak
    }
}
