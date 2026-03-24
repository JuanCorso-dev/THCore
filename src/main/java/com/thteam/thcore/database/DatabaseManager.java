package com.thteam.thcore.database;

import com.thteam.thcore.THCore;
import com.thteam.thcore.config.ConfigManager;
import com.thteam.thcore.database.impl.MySQLDatabase;
import com.thteam.thcore.database.impl.SQLiteDatabase;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Abstract database manager. Use {@link #create(THCore, ConfigManager)} to get the
 * correct implementation (MySQL or SQLite) based on config.yml.
 *
 * Usage from other plugins:
 *   DatabaseManager db = THCoreAPI.getDatabase();
 *   db.executeUpdate("INSERT INTO my_table (uuid, value) VALUES (?, ?)", uuid.toString(), 100);
 *
 *   db.executeQuery("SELECT * FROM my_table WHERE uuid = ?", rs -> {
 *       while (rs.next()) { ... }
 *   }, uuid.toString());
 */
public abstract class DatabaseManager {

    protected final THCore plugin;
    protected HikariDataSource dataSource;

    protected DatabaseManager(THCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Factory method — reads config and returns the correct implementation.
     */
    public static DatabaseManager create(THCore plugin, ConfigManager config) {
        String type = config.getString("database.type", "sqlite").toLowerCase().trim();
        return switch (type) {
            case "mysql" -> new MySQLDatabase(plugin, config);
            default -> new SQLiteDatabase(plugin, config);
        };
    }

    protected abstract HikariConfig buildHikariConfig();

    public abstract String getType();

    // ------------------------------------------------ lifecycle

    public void connect() {
        try {
            dataSource = new HikariDataSource(buildHikariConfig());
            plugin.getLogger().info("[Database] Connected (" + getType() + ")");
        } catch (Exception e) {
            plugin.getLogger().severe("[Database] Failed to connect (" + getType() + "): " + e.getMessage());
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            plugin.getLogger().info("[Database] Connection closed (" + getType() + ")");
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    // ------------------------------------------------ query helpers

    /**
     * Returns a raw Connection. Caller must close it (use try-with-resources).
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Executes an INSERT, UPDATE, or DELETE statement.
     * Params are set in order using PreparedStatement.setObject().
     */
    public void executeUpdate(String sql, Object... params) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] executeUpdate failed: " + e.getMessage());
        }
    }

    /**
     * Executes a SELECT and passes the ResultSet to a handler.
     * The Connection, PreparedStatement, and ResultSet are all closed automatically.
     *
     * Example:
     *   db.executeQuery("SELECT * FROM t WHERE uuid = ?", rs -> {
     *       while (rs.next()) { ... }
     *   }, uuid.toString());
     */
    public void executeQuery(String sql, QueryHandler handler, Object... params) throws SQLException {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                handler.handle(rs);
            }
        }
    }

    /** Functional interface for processing a ResultSet row by row. */
    @FunctionalInterface
    public interface QueryHandler {
        void handle(ResultSet rs) throws SQLException;
    }

    /**
     * Executes a SQL statement with no parameters (e.g. CREATE TABLE).
     */
    public void execute(String sql) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.execute();
        } catch (SQLException e) {
            plugin.getLogger().severe("[Database] execute failed: " + e.getMessage());
        }
    }
}
