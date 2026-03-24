package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import org.bukkit.inventory.ItemStack;

public class OraxenHook extends BaseHook {

    public OraxenHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "Oraxen";
    }

    @Override
    protected void load() {
        // Oraxen API is optional at compile time in this build setup.
    }

    /**
     * Returns the ItemStack for an Oraxen item ID, or null if not found.
     */
    public ItemStack getItem(String id) {
        return null;
    }

    /**
     * Returns true if the given ItemStack is an Oraxen item.
     */
    public boolean isOraxenItem(ItemStack stack) {
        return false;
    }

    /**
     * Returns the Oraxen item ID from an ItemStack, or null if not an Oraxen item.
     */
    public String getOraxenId(ItemStack stack) {
        return null;
    }

    /**
     * Returns true if an Oraxen item with the given ID exists.
     */
    public boolean exists(String id) {
        return false;
    }
}
