package com.thteam.thcore.gui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

/**
 * Represents a clickable button in a GUI: a slot + an ItemStack + an optional click handler.
 *
 * Use with BaseGUI.addButton() or FullGUI.addButton() to avoid boilerplate click dispatch.
 *
 * Examples:
 *
 *   // Button with click action
 *   addButton(new GUIButton(13, diamondItem, event -> player.sendMessage("Clicked!")));
 *
 *   // Decorative item (no action)
 *   addButton(new GUIButton(0, glassPane));
 *
 *   // Close button (FullGUI slot 81 = first hotbar slot)
 *   addButton(new GUIButton(81, barrierItem, event -> close()));
 */
public class GUIButton {

    private final int slot;
    private final ItemStack item;
    private final Consumer<InventoryClickEvent> onClick;

    /**
     * Creates a button with a click handler.
     *
     * @param slot    Inventory slot (0-based). For FullGUI use unified slots (0-89).
     * @param item    The item to display in the slot.
     * @param onClick Called when the player clicks this slot. May be null.
     */
    public GUIButton(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        this.slot = slot;
        this.item = item;
        this.onClick = onClick;
    }

    /**
     * Creates a decorative button with no click action.
     */
    public GUIButton(int slot, ItemStack item) {
        this(slot, item, null);
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getItem() {
        return item;
    }

    /**
     * Executes the click handler if one was provided.
     */
    public void click(InventoryClickEvent event) {
        if (onClick != null) {
            onClick.accept(event);
        }
    }

    public boolean hasAction() {
        return onClick != null;
    }
}
