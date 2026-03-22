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

import java.util.HashMap;
import java.util.Map;

/**
 * Abstract base class for chest-only inventory GUIs (no player inventory).
 *
 * Each instance represents ONE open GUI for ONE player.
 * Registers itself as a Bukkit listener on open() and unregisters on close.
 *
 * Supports GUIButton for quick slot → action mapping, or override handleClick()
 * for full manual control.
 *
 * Usage (with GUIButton):
 *
 *   public class MyShopGUI extends BaseGUI {
 *       public MyShopGUI(THCore plugin) {
 *           super(plugin, "<gold>Shop</gold>", 3);
 *           addButton(new GUIButton(13, diamondItem, e -> giveDiamond(e)));
 *           addButton(new GUIButton(22, closeItem,   e -> close()));
 *       }
 *
 *       @Override
 *       protected void fillItems() {
 *           fillEmpty(grayGlass); // decorate empty slots
 *       }
 *   }
 *
 * Usage (manual handleClick):
 *
 *   public class MyGUI extends BaseGUI {
 *       public MyGUI(THCore plugin) {
 *           super(plugin, "<aqua>My GUI</aqua>", 3);
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
 *   // Open:
 *   new MyGUI(plugin).open(player);
 */
public abstract class BaseGUI implements Listener {

    protected final THCore plugin;
    protected Inventory inventory;
    protected Player viewer;

    private final Component title;
    private final int rows;

    // GUIButton handlers registered via addButton()
    private final Map<Integer, GUIButton> buttonMap = new HashMap<>();

    /**
     * @param plugin  THCore instance (or your plugin's instance)
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
     * Creates the inventory, calls fillItems(), dispatches registered GUIButtons.
     */
    public void open(Player player) {
        this.viewer = player;
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        fillItems();

        // Place all registered GUIButton items
        for (GUIButton button : buttonMap.values()) {
            if (button.getSlot() >= 0 && button.getSlot() < inventory.getSize()) {
                inventory.setItem(button.getSlot(), button.getItem());
            }
        }

        player.openInventory(inventory);
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Programmatically closes the GUI.
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
     * GUIButton items are placed automatically after this method returns.
     */
    protected abstract void fillItems();

    /**
     * Called when the player clicks inside this GUI's inventory.
     * The event is already cancelled. GUIButton handlers fire before this.
     * Override for manual slot-based logic.
     */
    protected void handleClick(InventoryClickEvent event) {}

    /**
     * Called when the GUI is closed (player or programmatic).
     * Override to save data or run cleanup.
     */
    protected void handleClose(Player player) {}

    // ------------------------------------------------ Item helpers

    /**
     * Places an item in the given slot (0-based, chest slots only).
     */
    protected void setItem(int slot, ItemStack item) {
        if (inventory != null && slot >= 0 && slot < inventory.getSize()) {
            inventory.setItem(slot, item);
        }
    }

    /**
     * Registers a GUIButton.
     * Its item is placed in the slot, and its Consumer fires on click automatically.
     */
    protected void addButton(GUIButton button) {
        buttonMap.put(button.getSlot(), button);
        // If inventory already exists (called after open), place the item immediately
        if (inventory != null && button.getSlot() >= 0 && button.getSlot() < inventory.getSize()) {
            inventory.setItem(button.getSlot(), button.getItem());
        }
    }

    /**
     * Fills every empty slot with the given filler item.
     * Useful for decorating blank spots with gray glass panes.
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
     * Clears the inventory and re-runs fillItems() + GUIButtons.
     * Useful for paginated or dynamic GUIs.
     */
    protected void refresh() {
        if (inventory == null) return;
        inventory.clear();
        fillItems();
        for (GUIButton button : buttonMap.values()) {
            if (button.getSlot() >= 0 && button.getSlot() < inventory.getSize()) {
                inventory.setItem(button.getSlot(), button.getItem());
            }
        }
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
        event.setCancelled(true);

        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(inventory)) return;

        // Dispatch GUIButton handler first
        GUIButton button = buttonMap.get(event.getSlot());
        if (button != null) {
            button.click(event);
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
        handleClose((Player) event.getPlayer());
        HandlerList.unregisterAll(this);
    }
}
