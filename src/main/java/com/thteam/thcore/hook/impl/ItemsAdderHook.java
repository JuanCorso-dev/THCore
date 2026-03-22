package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import dev.lone.itemsadder.api.CustomStack;
import dev.lone.itemsadder.api.Events.ItemsAdderLoadDataEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderHook extends BaseHook implements Listener {

    private boolean dataLoaded = false;

    public ItemsAdderHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "ItemsAdder";
    }

    @Override
    protected void load() {
        // ItemsAdder loads its items asynchronously after the server starts.
        // Register a listener to know when items are fully available.
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Returns true when ItemsAdder has finished loading all custom items.
     * Wait for this before calling getItem().
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    @EventHandler
    public void onItemsAdderLoad(ItemsAdderLoadDataEvent event) {
        dataLoaded = true;
        plugin.getLogger().info("[Hook] ItemsAdder data fully loaded.");
    }

    /**
     * Returns the ItemStack for a namespaced ItemsAdder ID (e.g. "namespace:item_id"),
     * or null if not found.
     */
    public ItemStack getItem(String namespacedId) {
        CustomStack stack = CustomStack.getInstance(namespacedId);
        return stack != null ? stack.getItemStack() : null;
    }

    /**
     * Returns true if the given ItemStack is an ItemsAdder custom item.
     */
    public boolean isItemsAdderItem(ItemStack stack) {
        return CustomStack.byItemStack(stack) != null;
    }

    /**
     * Returns the namespaced ID (e.g. "namespace:item_id") from an ItemStack,
     * or null if it is not an ItemsAdder item.
     */
    public String getItemsAdderId(ItemStack stack) {
        CustomStack cs = CustomStack.byItemStack(stack);
        return cs != null ? cs.getNamespacedID() : null;
    }
}
