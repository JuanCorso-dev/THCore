package com.thteam.thcore.database.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.config.ConfigManager;
import com.thteam.thcore.database.DatabaseManager;
import com.zaxxer.hikari.HikariConfig;

import java.io.File;

public class SQLiteDatabase extends DatabaseManager {

    private final ConfigManager config;

    public SQLiteDatabase(THCore plugin, ConfigManager config) {
        super(plugin);
        this.config = config;
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        String fileName = config.getString("database.sqlite.file", "thcore.db");
        File dbFile = new File(plugin.getDataFolder(), fileName);

        // Ensure parent directory exists
        dbFile.getParentFile().mkdirs();

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        hikari.setDriverClassName("org.sqlite.JDBC");

        // SQLite only supports one writer at a time; keep pool at 1
        hikari.setMaximumPoolSize(1);
        hikari.setMinimumIdle(1);
        hikari.setConnectionTestQuery("SELECT 1");
        hikari.setPoolName("THCore-SQLite");

        // Recommended SQLite pragmas for performance
        hikari.addDataSourceProperty("journal_mode", "WAL");
        hikari.addDataSourceProperty("synchronous", "NORMAL");

        return hikari;
    }

    @Override
    public String getType() {
        return "SQLite";
    }
}
