package com.thteam.thcore.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player cooldowns identified by a string key.
 * Cooldowns are stored in memory; they reset on server restart.
 *
 * Usage:
 *   cooldownManager.setCooldown(player.getUniqueId(), "my_skill", 5000L);
 *   if (cooldownManager.hasCooldown(player.getUniqueId(), "my_skill")) { ... }
 */
public class CooldownManager {

    // UUID → (key → expiry timestamp in ms)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    /**
     * Sets a cooldown for a player.
     *
     * @param uuid            Player UUID
     * @param key             Cooldown identifier (e.g. "teleport", "skill_fire")
     * @param durationMillis  Duration in milliseconds
     */
    public void setCooldown(UUID uuid, String key, long durationMillis) {
        cooldowns
            .computeIfAbsent(uuid, k -> new HashMap<>())
            .put(key, System.currentTimeMillis() + durationMillis);
    }

    /**
     * Returns true if the player is still on cooldown for the given key.
     * Expired entries are cleaned up automatically.
     */
    public boolean hasCooldown(UUID uuid, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return false;

        Long expiry = playerCooldowns.get(key);
        if (expiry == null) return false;

        if (System.currentTimeMillis() >= expiry) {
            playerCooldowns.remove(key);
            return false;
        }
        return true;
    }

    /**
     * Returns the remaining cooldown time in milliseconds, or 0 if not on cooldown.
     */
    public long getRemaining(UUID uuid, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return 0L;

        Long expiry = playerCooldowns.get(key);
        if (expiry == null) return 0L;

        return Math.max(0L, expiry - System.currentTimeMillis());
    }

    /**
     * Returns the remaining cooldown time in seconds (rounded up), or 0.
     */
    public long getRemainingSeconds(UUID uuid, String key) {
        long ms = getRemaining(uuid, key);
        return (long) Math.ceil(ms / 1000.0);
    }

    /**
     * Removes a specific cooldown for a player.
     */
    public void clearCooldown(UUID uuid, String key) {
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(key);
        }
    }

    /**
     * Removes all cooldowns for a player.
     */
    public void clearAll(UUID uuid) {
        cooldowns.remove(uuid);
    }
}
