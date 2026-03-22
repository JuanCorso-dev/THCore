package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.items.ItemBuilder;
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
        // Verify API is accessible
        OraxenItems.exists("__thcore_test__");
    }

    /**
     * Returns the ItemStack for an Oraxen item ID, or null if not found.
     */
    public ItemStack getItem(String id) {
        ItemBuilder builder = OraxenItems.getItemById(id);
        return builder != null ? builder.build() : null;
    }

    /**
     * Returns true if the given ItemStack is an Oraxen item.
     */
    public boolean isOraxenItem(ItemStack stack) {
        return OraxenItems.getIdByItem(stack) != null;
    }

    /**
     * Returns the Oraxen item ID from an ItemStack, or null if not an Oraxen item.
     */
    public String getOraxenId(ItemStack stack) {
        return OraxenItems.getIdByItem(stack);
    }

    /**
     * Returns true if an Oraxen item with the given ID exists.
     */
    public boolean exists(String id) {
        return OraxenItems.exists(id);
    }
}
