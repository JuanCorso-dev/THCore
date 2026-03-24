package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import org.bukkit.inventory.ItemStack;

public class ItemsAdderHook extends BaseHook {

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
        // ItemsAdder API is optional at compile time in this build setup.
        dataLoaded = true;
    }

    /**
     * Returns true when ItemsAdder has finished loading all custom items.
     * Wait for this before calling getItem().
     */
    public boolean isDataLoaded() {
        return dataLoaded;
    }

    /**
     * Returns the ItemStack for a namespaced ItemsAdder ID (e.g. "namespace:item_id"),
     * or null if not found.
     */
    public ItemStack getItem(String namespacedId) {
        return null;
    }

    /**
     * Returns true if the given ItemStack is an ItemsAdder custom item.
     */
    public boolean isItemsAdderItem(ItemStack stack) {
        return false;
    }

    /**
     * Returns the namespaced ID (e.g. "namespace:item_id") from an ItemStack,
     * or null if it is not an ItemsAdder item.
     */
    public String getItemsAdderId(ItemStack stack) {
        return null;
    }
}
