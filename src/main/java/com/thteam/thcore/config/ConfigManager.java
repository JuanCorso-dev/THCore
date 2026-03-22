package com.thteam.thcore.config;

import com.thteam.thcore.THCore;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class ConfigManager {

    private final THCore plugin;
    private FileConfiguration config;

    public ConfigManager(THCore plugin) {
        this.plugin = plugin;
    }

    public void load() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public void reload() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getString(String path, String def) {
        return config.getString(path, def);
    }

    public int getInt(String path, int def) {
        return config.getInt(path, def);
    }

    public long getLong(String path, long def) {
        return config.getLong(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        return config.getBoolean(path, def);
    }

    public double getDouble(String path, double def) {
        return config.getDouble(path, def);
    }

    public ConfigurationSection getSection(String path) {
        return config.getConfigurationSection(path);
    }

    public FileConfiguration getRaw() {
        return config;
    }

    public List<String> getStringList(String path) {
        return config.getStringList(path);
    }

    public boolean contains(String path) {
        return config.contains(path);
    }

    /**
     * Sets a value in the in-memory config.
     * Call {@link #save()} afterwards to persist to disk.
     */
    public void set(String path, Object value) {
        config.set(path, value);
    }

    /**
     * Saves the current in-memory config to config.yml on disk.
     */
    public void save() {
        plugin.saveConfig();
    }
}
