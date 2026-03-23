package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.query.QueryOptions;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Soft-depend hook for LuckPerms.
 *
 * Provides sync access to the most common LuckPerms operations via
 * PlayerAdapter (no async I/O — data is already cached per player).
 *
 * Usage:
 *   LuckPermsHook lp = THCoreAPI.getLuckPerms();
 *   if (lp != null) {
 *       String group  = lp.getPrimaryGroup(player);
 *       String prefix = lp.getPrefix(player);
 *       boolean isVip = lp.isInGroup(player, "vip");
 *   }
 */
public class LuckPermsHook extends BaseHook {

    private LuckPerms api;

    public LuckPermsHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "LuckPerms";
    }

    @Override
    protected void load() throws Exception {
        api = LuckPermsProvider.get();
    }

    // ---------------------------------------------------------------- Groups

    /**
     * Returns the primary group of the player.
     */
    public String getPrimaryGroup(Player player) {
        return getUser(player).getPrimaryGroup();
    }

    /**
     * Returns all groups the player belongs to (direct assignments only).
     * Does not include inherited/parent groups.
     */
    public Set<String> getGroups(Player player) {
        return getUser(player).getNodes()
            .stream()
            .filter(NodeType.INHERITANCE::matches)
            .map(NodeType.INHERITANCE::cast)
            .map(InheritanceNode::getGroupName)
            .collect(Collectors.toSet());
    }

    /**
     * Returns true if the player is in the given group (case-insensitive).
     * Checks both the primary group and all directly assigned groups.
     */
    public boolean isInGroup(Player player, String group) {
        if (getPrimaryGroup(player).equalsIgnoreCase(group)) return true;
        return getGroups(player).stream().anyMatch(g -> g.equalsIgnoreCase(group));
    }

    // ---------------------------------------------------------------- Meta

    /**
     * Returns the player's prefix as calculated by LuckPerms (context-aware).
     * Returns null if no prefix is set.
     */
    public String getPrefix(Player player) {
        return getMetaData(player).getPrefix();
    }

    /**
     * Returns the player's suffix as calculated by LuckPerms (context-aware).
     * Returns null if no suffix is set.
     */
    public String getSuffix(Player player) {
        return getMetaData(player).getSuffix();
    }

    /**
     * Returns the value of a custom meta key for the player.
     * Returns null if the key is not set.
     *
     * Example: getMetaValue(player, "level") for `meta.level.5`
     */
    public String getMetaValue(Player player, String key) {
        return getMetaData(player).getMetaValue(key);
    }

    // ---------------------------------------------------------------- Raw access

    /**
     * Returns the raw LuckPerms API for advanced operations (async user loading, etc.).
     */
    public LuckPerms getAPI() {
        return api;
    }

    /**
     * Returns the cached User object for the given player.
     * The player must be online — offline player data requires async loading via UserManager.
     */
    public User getUser(Player player) {
        return api.getPlayerAdapter(Player.class).getUser(player);
    }

    // ---------------------------------------------------------------- Internal helpers

    private net.luckperms.api.cacheddata.CachedMetaData getMetaData(Player player) {
        User user = getUser(player);
        QueryOptions options = api.getPlayerAdapter(Player.class).getQueryOptions(player);
        return user.getCachedData().getMetaData(options);
    }
}
