package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;

import java.util.UUID;

public class PlayerPointsHook extends BaseHook {

    private PlayerPointsAPI api;

    public PlayerPointsHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "PlayerPoints";
    }

    @Override
    protected void load() throws Exception {
        PlayerPoints pp = (PlayerPoints) plugin.getServer()
            .getPluginManager().getPlugin("PlayerPoints");
        if (pp == null)
            throw new Exception("PlayerPoints instance is null");
        api = pp.getAPI();
    }

    /**
     * Returns the points balance of a player.
     */
    public int getPoints(UUID uuid) {
        return api.look(uuid);
    }

    /**
     * Gives points to a player.
     */
    public boolean givePoints(UUID uuid, int amount) {
        return api.give(uuid, amount);
    }

    /**
     * Takes points from a player.
     */
    public boolean takePoints(UUID uuid, int amount) {
        return api.take(uuid, amount);
    }

    /**
     * Sets a player's points to the given amount.
     */
    public boolean setPoints(UUID uuid, int amount) {
        return api.set(uuid, amount);
    }

    /**
     * Returns true if the player has at least the given amount of points.
     */
    public boolean hasPoints(UUID uuid, int amount) {
        return api.look(uuid) >= amount;
    }

    /** Raw API access for advanced usage. */
    public PlayerPointsAPI getAPI() {
        return api;
    }
}
