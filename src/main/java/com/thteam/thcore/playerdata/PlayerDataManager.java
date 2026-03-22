package com.thteam.thcore.playerdata;

import com.thteam.thcore.THCore;
import com.thteam.thcore.database.DatabaseManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages per-player key-value data persisted in the database.
 *
 * Table: thcore_player_data (uuid, key, value)
 * Created automatically on first use.
 *
 * Usage:
 *   PlayerDataManager dm = THCoreAPI.getPlayerDataManager();
 *   PlayerData data = dm.load(player.getUniqueId());
 *   data.set("coins", 500);
 *   data.saveAsync(plugin);
 */
public class PlayerDataManager {

    private final THCore plugin;
    private final DatabaseManager db;

    // ---------------------------------------------------------------- Constructor

    public PlayerDataManager(THCore plugin) {
        this.plugin = plugin;
        this.db     = plugin.getDatabaseManager();
        createTable();
    }

    // ---------------------------------------------------------------- Table creation

    private void createTable() {
        db.execute(
            "CREATE TABLE IF NOT EXISTS thcore_player_data (" +
            "  uuid  VARCHAR(36) NOT NULL," +
            "  key   VARCHAR(64) NOT NULL," +
            "  value TEXT," +
            "  PRIMARY KEY (uuid, key)" +
            ")"
        );
    }

    // ---------------------------------------------------------------- Public API

    /**
     * Loads all data for the given UUID from the database.
     * Returns an empty PlayerData object if no data exists.
     * This is a blocking call — run it async if needed.
     */
    public PlayerData load(UUID uuid) {
        Map<String, String> values = new HashMap<>();
        try (ResultSet rs = db.executeQuery(
                "SELECT key, value FROM thcore_player_data WHERE uuid = ?",
                uuid.toString())) {
            while (rs.next()) {
                values.put(rs.getString("key"), rs.getString("value"));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("[PlayerData] Failed to load " + uuid + ": " + e.getMessage());
        }
        return new PlayerData(uuid, values, this);
    }

    /**
     * Saves all data from the PlayerData object to the database.
     * Uses UPSERT (INSERT OR REPLACE for SQLite, INSERT ... ON DUPLICATE KEY UPDATE for MySQL).
     * This is a blocking call — use PlayerData.saveAsync() from the main thread.
     */
    public void save(PlayerData data) {
        String uuid = data.getUUID().toString();
        String upsert = buildUpsertQuery();

        for (Map.Entry<String, String> entry : data.getRawData().entrySet()) {
            db.executeUpdate(upsert, uuid, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Deletes all stored data for the given UUID.
     */
    public void delete(UUID uuid) {
        db.executeUpdate(
            "DELETE FROM thcore_player_data WHERE uuid = ?",
            uuid.toString()
        );
    }

    // ---------------------------------------------------------------- Internal helpers

    /**
     * Returns the correct UPSERT statement for the current database type.
     * SQLite: INSERT OR REPLACE
     * MySQL:  INSERT ... ON DUPLICATE KEY UPDATE
     */
    private String buildUpsertQuery() {
        if ("mysql".equalsIgnoreCase(db.getType())) {
            return "INSERT INTO thcore_player_data (uuid, key, value) VALUES (?, ?, ?) " +
                   "ON DUPLICATE KEY UPDATE value = VALUES(value)";
        }
        // SQLite (default)
        return "INSERT OR REPLACE INTO thcore_player_data (uuid, key, value) VALUES (?, ?, ?)";
    }
}
