package com.thteam.thcore.hook.impl;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WorldGuardHook extends BaseHook {

    private RegionContainer regionContainer;

    public WorldGuardHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "WorldGuard";
    }

    @Override
    protected void load() {
        regionContainer = WorldGuard.getInstance().getPlatform().getRegionContainer();
    }

    /**
     * Returns true if the player is inside the given region.
     */
    public boolean isInRegion(Player player, String regionId) {
        return isInRegion(player.getLocation(), regionId);
    }

    /**
     * Returns true if the location is inside the given region.
     */
    public boolean isInRegion(Location location, String regionId) {
        RegionManager manager = getRegionManager(location);
        if (manager == null) return false;

        BlockVector3 pos = BukkitAdapter.asBlockVector(location);
        for (ProtectedRegion region : manager.getApplicableRegions(pos)) {
            if (region.getId().equalsIgnoreCase(regionId)) return true;
        }
        return false;
    }

    /**
     * Returns all region IDs at the given location.
     */
    public Set<String> getRegionsAt(Location location) {
        RegionManager manager = getRegionManager(location);
        if (manager == null) return Collections.emptySet();

        BlockVector3 pos = BukkitAdapter.asBlockVector(location);
        Set<String> ids = new HashSet<>();
        manager.getApplicableRegions(pos).forEach(r -> ids.add(r.getId()));
        return ids;
    }

    /**
     * Returns all region IDs at the player's current location.
     */
    public Set<String> getRegionsAt(Player player) {
        return getRegionsAt(player.getLocation());
    }

    /**
     * Returns the raw RegionContainer for advanced usage.
     */
    public RegionContainer getRegionContainer() {
        return regionContainer;
    }

    private RegionManager getRegionManager(Location location) {
        if (location.getWorld() == null) return null;
        return regionContainer.get(BukkitAdapter.adapt(location.getWorld()));
    }
}
