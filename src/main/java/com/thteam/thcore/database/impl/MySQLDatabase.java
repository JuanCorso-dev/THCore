package com.thteam.thcore.database.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.config.ConfigManager;
import com.thteam.thcore.database.DatabaseManager;
import com.zaxxer.hikari.HikariConfig;

public class MySQLDatabase extends DatabaseManager {

    private final ConfigManager config;

    public MySQLDatabase(THCore plugin, ConfigManager config) {
        super(plugin);
        this.config = config;
    }

    @Override
    protected HikariConfig buildHikariConfig() {
        String host     = config.getString("database.mysql.host", "localhost");
        int    port     = config.getInt("database.mysql.port", 3306);
        String db       = config.getString("database.mysql.database", "thcore");
        String user     = config.getString("database.mysql.username", "root");
        String password = config.getString("database.mysql.password", "");
        boolean ssl     = config.getBoolean("database.mysql.use-ssl", false);

        HikariConfig hikari = new HikariConfig();
        hikari.setJdbcUrl(
            "jdbc:mysql://" + host + ":" + port + "/" + db
            + "?useSSL=" + ssl
            + "&autoReconnect=true"
            + "&characterEncoding=utf8"
            + "&serverTimezone=UTC"
        );
        hikari.setUsername(user);
        hikari.setPassword(password);
        hikari.setDriverClassName("com.mysql.cj.jdbc.Driver");

        hikari.setMaximumPoolSize(config.getInt("database.mysql.pool-size", 10));
        hikari.setConnectionTimeout(config.getLong("database.mysql.connection-timeout", 30000));
        hikari.setIdleTimeout(config.getLong("database.mysql.idle-timeout", 600000));
        hikari.setMaxLifetime(config.getLong("database.mysql.max-lifetime", 1800000));
        hikari.setPoolName("THCore-MySQL");

        // Performance-recommended properties
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "250");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari.addDataSourceProperty("useServerPrepStmts", "true");

        return hikari;
    }

    @Override
    public String getType() {
        return "MySQL";
    }
}
