package com.thteam.thcore.message;

import com.thteam.thcore.THCore;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Manages configurable messages and the plugin prefix.
 * Reads prefix and message templates from config.yml.
 *
 * Usage from other plugins:
 *   THCoreAPI.getMessageManager().send(player, "<green>Done!</green>");
 */
public class MessageManager {

    private final THCore plugin;
    private String prefix;

    public MessageManager(THCore plugin) {
        this.plugin = plugin;
        loadPrefix();
    }

    public void reload() {
        loadPrefix();
    }

    private void loadPrefix() {
        this.prefix = plugin.getConfigManager().getString(
            "messages.prefix",
            "<gray>[<aqua>THCore</aqua>]</gray> "
        );
    }

    // ------------------------------------------------ send helpers

    /** Sends a colorized message to any CommandSender, with prefix. */
    public void send(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.colorize(prefix + message));
    }

    /** Sends a colorized message WITHOUT the plugin prefix. */
    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(MessageUtil.colorize(message));
    }

    /** Sends a pre-built Component directly. */
    public void send(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

    /** Broadcasts a message (with prefix) to all online players and console. */
    public void broadcast(String message) {
        Bukkit.broadcast(MessageUtil.colorize(prefix + message));
    }

    // ------------------------------------------------ config message shortcuts

    public void sendNoPermission(CommandSender sender) {
        sendRaw(sender, plugin.getConfigManager().getString(
            "messages.no-permission",
            "<red>No tienes permiso.</red>"
        ));
    }

    public void sendReloadSuccess(CommandSender sender) {
        sendRaw(sender, plugin.getConfigManager().getString(
            "messages.reload-success",
            "<green>Configuración recargada.</green>"
        ));
    }

    public void sendOnlyPlayers(CommandSender sender) {
        sendRaw(sender, plugin.getConfigManager().getString(
            "messages.only-players",
            "<red>Solo jugadores pueden usar este comando.</red>"
        ));
    }

    // ------------------------------------------------ utility

    /** Returns the raw prefix string (MiniMessage format). */
    public String getPrefix() {
        return prefix;
    }

    /** Colorizes a string and returns it as a Component. */
    public Component colorize(String text) {
        return MessageUtil.colorize(text);
    }
}
