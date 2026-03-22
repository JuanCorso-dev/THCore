package com.thteam.thcore.hook.impl;

import com.nexomc.nexo.api.NexoBlocks;
import com.nexomc.nexo.api.NexoFurniture;
import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import org.bukkit.inventory.ItemStack;

public class NexoHook extends BaseHook {

    public NexoHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "Nexo";
    }

    @Override
    protected void load() {
        // Verify the API is accessible — NexoItems uses static access
        NexoItems.exists("__thcore_test__"); // returns false, won't throw if API is up
    }

    /**
     * Returns the ItemStack for a Nexo item ID, or null if not found.
     */
    public ItemStack getItem(String id) {
        ItemBuilder builder = NexoItems.itemFromId(id);
        return builder != null ? builder.build() : null;
    }

    /**
     * Returns true if the given ItemStack is a Nexo item.
     */
    public boolean isNexoItem(ItemStack stack) {
        return NexoItems.idFromItem(stack) != null;
    }

    /**
     * Returns the Nexo item ID from an ItemStack, or null if not a Nexo item.
     */
    public String getNexoId(ItemStack stack) {
        return NexoItems.idFromItem(stack);
    }

    /**
     * Returns true if a Nexo item with the given ID exists.
     */
    public boolean exists(String id) {
        return NexoItems.exists(id);
    }

    /** Raw access for advanced usage. */
    public NexoItems getNexoItems() {
        return null; // NexoItems is purely static
    }

    public NexoBlocks getNexoBlocks() {
        return null; // NexoBlocks is purely static
    }

    public NexoFurniture getNexoFurniture() {
        return null; // NexoFurniture is purely static
    }
}
