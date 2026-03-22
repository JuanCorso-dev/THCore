package com.thteam.thcore.playerdata;

import com.thteam.thcore.THCore;
import com.thteam.thcore.util.Tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Holds per-player key-value data loaded from the database.
 *
 * All values are stored as strings internally and cast on read.
 * Use {@link #save()} or {@link #saveAsync(THCore)} to persist changes.
 *
 * Usage:
 *   PlayerData data = THCoreAPI.getPlayerDataManager().load(player.getUniqueId());
 *   data.set("kills", 10);
 *   data.set("rank", "VIP");
 *   int kills = data.getInt("kills", 0);
 *   data.saveAsync(plugin);
 */
public class PlayerData {

    private final UUID uuid;
    private final Map<String, String> data;
    private boolean dirty = false;

    private PlayerDataManager manager;

    // ---------------------------------------------------------------- Constructor (package-private)

    PlayerData(UUID uuid, Map<String, String> data, PlayerDataManager manager) {
        this.uuid    = uuid;
        this.data    = new HashMap<>(data);
        this.manager = manager;
    }

    // ---------------------------------------------------------------- Setters

    /**
     * Sets a value for the given key. Marks the data as dirty (unsaved).
     * The value is stored as its String representation.
     */
    public void set(String key, Object value) {
        data.put(key, String.valueOf(value));
        dirty = true;
    }

    /**
     * Removes a key entirely. Marks the data as dirty.
     */
    public void remove(String key) {
        if (data.containsKey(key)) {
            data.remove(key);
            dirty = true;
        }
    }

    // ---------------------------------------------------------------- Getters

    /** Returns true if this key exists. */
    public boolean has(String key) {
        return data.containsKey(key);
    }

    /** Returns the raw string value, or the default if the key does not exist. */
    public String getString(String key, String def) {
        return data.getOrDefault(key, def);
    }

    /** Returns the value as an int, or the default if missing or not parseable. */
    public int getInt(String key, int def) {
        String val = data.get(key);
        if (val == null) return def;
        try { return Integer.parseInt(val); } catch (NumberFormatException e) { return def; }
    }

    /** Returns the value as a long, or the default if missing or not parseable. */
    public long getLong(String key, long def) {
        String val = data.get(key);
        if (val == null) return def;
        try { return Long.parseLong(val); } catch (NumberFormatException e) { return def; }
    }

    /** Returns the value as a double, or the default if missing or not parseable. */
    public double getDouble(String key, double def) {
        String val = data.get(key);
        if (val == null) return def;
        try { return Double.parseDouble(val); } catch (NumberFormatException e) { return def; }
    }

    /** Returns the value as a boolean, or the default if missing. */
    public boolean getBoolean(String key, boolean def) {
        String val = data.get(key);
        if (val == null) return def;
        return Boolean.parseBoolean(val);
    }

    // ---------------------------------------------------------------- Persistence

    /**
     * Saves all dirty data to the database synchronously.
     * Call on the main thread only if you don't mind a brief stall;
     * prefer {@link #saveAsync(THCore)} for most cases.
     */
    public void save() {
        if (!dirty) return;
        manager.save(this);
        dirty = false;
    }

    /**
     * Saves all dirty data to the database asynchronously (non-blocking).
     */
    public void saveAsync(THCore plugin) {
        if (!dirty) return;
        dirty = false; // mark clean before async to avoid double-saves
        Tasks.async(plugin, () -> manager.save(this));
    }

    // ---------------------------------------------------------------- Internal

    /** Returns the player's UUID. */
    public UUID getUUID() { return uuid; }

    /** Returns true if there are unsaved changes. */
    public boolean isDirty() { return dirty; }

    /** Package-private: raw map for the manager to iterate. */
    Map<String, String> getRawData() { return data; }
}
