package com.thteam.thcore.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Represents a clickable (or decorative) button in a GUI.
 *
 * Supports a fluent builder API:
 *
 *   new GUIButton(13, diamondItem)
 *       .priority(10)
 *       .require(GUIRequirement.permission("vip"))         // only shown to VIPs
 *       .requireClick(GUIRequirement.money(100))           // click only if has $100
 *       .onDeny(GUIAction.message("<red>Need $100!"))      // runs if click req fails
 *       .onClick(GUIAction.takeMoney(100),
 *                GUIAction.console("give %player% diamond 1"))
 *
 * Priority: when multiple buttons share the same slot, the one with the highest
 * priority whose viewRequirement passes is displayed. Lower priorities act as fallbacks.
 */
public class GUIButton {

    private final int slot;
    private final ItemStack item;

    // Priority: higher = evaluated first when multiple buttons share a slot
    private int priority = 0;

    // View requirement: if set, this button is only shown when it passes
    private GUIRequirement viewRequirement = null;

    // Click requirement: if set, click actions only run when it passes
    private GUIRequirement clickRequirement = null;

    // Actions run when click requirement FAILS
    private final List<GUIAction> denyActions = new ArrayList<>();

    // Actions run when the button is clicked (and click requirement passes)
    private final List<GUIAction> clickActions = new ArrayList<>();

    // Legacy Consumer support (kept for backwards compatibility)
    private Consumer<InventoryClickEvent> legacyOnClick = null;

    // ================================================================
    // Constructors (backwards-compatible)
    // ================================================================

    /**
     * Decorative button — no click action.
     */
    public GUIButton(int slot, ItemStack item) {
        this.slot = slot;
        this.item = item;
    }

    /**
     * Button with a legacy Consumer<InventoryClickEvent> handler.
     * Kept for backwards compatibility with existing code.
     */
    public GUIButton(int slot, ItemStack item, Consumer<InventoryClickEvent> onClick) {
        this.slot = slot;
        this.item = item;
        this.legacyOnClick = onClick;
    }

    // ================================================================
    // Builder methods (fluent API, return this)
    // ================================================================

    /**
     * Sets the priority of this button.
     * When multiple buttons target the same slot, the highest-priority visible one wins.
     * Default is 0.
     */
    public GUIButton priority(int p) {
        this.priority = p;
        return this;
    }

    /**
     * Sets a view requirement. The button is only shown if this passes for the viewer.
     */
    public GUIButton require(GUIRequirement req) {
        this.viewRequirement = req;
        return this;
    }

    /**
     * Sets a click requirement. Click actions only run if this passes.
     * If it fails, denyActions run instead.
     */
    public GUIButton requireClick(GUIRequirement req) {
        this.clickRequirement = req;
        return this;
    }

    /**
     * Adds actions to run when the click requirement FAILS.
     */
    public GUIButton onDeny(GUIAction... actions) {
        denyActions.addAll(Arrays.asList(actions));
        return this;
    }

    /**
     * Adds GUIAction-based click handlers.
     * These replace/supplement the legacy Consumer if both are set.
     */
    public GUIButton onClick(GUIAction... actions) {
        clickActions.addAll(Arrays.asList(actions));
        return this;
    }

    // ================================================================
    // Evaluation methods
    // ================================================================

    /**
     * Returns true if this button should be visible to the given player.
     * A button with no view requirement is always visible.
     */
    public boolean isVisible(Player player) {
        return viewRequirement == null || viewRequirement.test(player);
    }

    /**
     * Returns true if the player is allowed to trigger click actions.
     * A button with no click requirement always allows clicks.
     */
    public boolean canClick(Player player) {
        return clickRequirement == null || clickRequirement.test(player);
    }

    /**
     * Executes all deny actions (called when clickRequirement fails).
     */
    public void executeDenyActions(Player player) {
        for (GUIAction action : denyActions) {
            action.execute(player);
        }
    }

    /**
     * Executes all click actions (called when click is allowed).
     * Also fires the legacy Consumer<InventoryClickEvent> if set.
     */
    public void executeClickActions(Player player, InventoryClickEvent event) {
        for (GUIAction action : clickActions) {
            action.execute(player);
        }
        if (legacyOnClick != null) {
            legacyOnClick.accept(event);
        }
    }

    /**
     * Full click dispatch: checks requirement → runs deny or click actions.
     */
    public void click(Player player, InventoryClickEvent event) {
        if (canClick(player)) {
            executeClickActions(player, event);
        } else {
            executeDenyActions(player);
        }
    }

    /**
     * Legacy click dispatch (no Player reference).
     * Kept for backwards compatibility — prefer click(Player, InventoryClickEvent).
     */
    public void click(InventoryClickEvent event) {
        click((Player) event.getWhoClicked(), event);
    }

    // ================================================================
    // Getters
    // ================================================================

    public int getSlot() { return slot; }
    public ItemStack getItem() { return item; }
    public int getPriority() { return priority; }
    public boolean hasAction() { return !clickActions.isEmpty() || legacyOnClick != null; }
}
