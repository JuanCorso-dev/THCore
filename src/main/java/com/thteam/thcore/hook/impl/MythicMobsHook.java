package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;

public class MythicMobsHook extends BaseHook {

    private MythicBukkit api;

    public MythicMobsHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "MythicMobs";
    }

    @Override
    protected void load() {
        api = MythicBukkit.inst();
    }

    /**
     * Spawns a MythicMobs mob at the given location.
     *
     * @param mobType   The internal MythicMobs mob type name
     * @param location  Spawn location
     * @param level     Mob level
     * @return true if the mob was spawned successfully
     */
    public boolean spawnMob(String mobType, Location location, double level) {
        return api.getMobManager()
            .spawnMob(mobType, BukkitAdapter.adapt(location), level)
            .isPresent();
    }

    /**
     * Returns true if the entity is an active MythicMobs mob.
     */
    public boolean isMythicMob(LivingEntity entity) {
        return api.getMobManager().isActiveMob(BukkitAdapter.adapt(entity));
    }

    /**
     * Returns the MythicMobs internal mob type name for an entity,
     * or null if the entity is not a MythicMobs mob.
     */
    public String getMobType(LivingEntity entity) {
        return api.getMobManager()
            .getMythicMobInstance(BukkitAdapter.adapt(entity))
            .map(m -> m.getType().getInternalName())
            .orElse(null);
    }

    /**
     * Returns true if a mob type with the given name exists in MythicMobs config.
     */
    public boolean mobExists(String mobType) {
        return api.getMobManager().getMythicMob(mobType).isPresent();
    }

    /** Raw API access for advanced usage. */
    public MythicBukkit getAPI() {
        return api;
    }
}
