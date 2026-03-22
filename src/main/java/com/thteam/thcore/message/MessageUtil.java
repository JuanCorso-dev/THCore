package com.thteam.thcore.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;

/**
 * Static utility for color/format handling using MiniMessage (Paper 1.21.1 native).
 *
 * Supports:
 *   - MiniMessage tags:  <red>, <bold>, <#FF0000>, <gradient:red:blue>
 *   - Legacy & codes:    &c, &l, &r
 *   - Legacy hex:        &#FF0000
 */
public final class MessageUtil {

    private static final MiniMessage MINI = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY =
        LegacyComponentSerializer.builder()
            .character('&')
            .hexColors()
            .useUnusualXRepeatedCharacterHexFormat()
            .build();

    private MessageUtil() {}

    /**
     * Converts a string with MiniMessage and/or legacy &-codes into a Component.
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // First deserialize legacy & codes into a Component, then serialize that
        // Component back to a MiniMessage string so we can feed it through MiniMessage.
        // This allows mixing both: "&cHello <bold>world</bold>"
        String miniText = MINI.serialize(LEGACY.deserialize(text));
        return MINI.deserialize(miniText);
    }

    /**
     * Sends a colorized message to a CommandSender.
     */
    public static void send(CommandSender sender, String message) {
        sender.sendMessage(colorize(message));
    }

    /**
     * Sends a colorized message with a prefix prepended.
     */
    public static void send(CommandSender sender, String prefix, String message) {
        sender.sendMessage(colorize(prefix + message));
    }

    /**
     * Strips all color/format codes and returns a plain string.
     * Useful for inventory titles and log messages.
     */
    public static String stripColors(String text) {
        return PlainTextComponentSerializer.plainText().serialize(colorize(text));
    }

    /**
     * Returns the colorized text as a legacy §-coded string.
     * For APIs that still require legacy strings (some hook APIs).
     */
    public static String toLegacy(String text) {
        return LegacyComponentSerializer.legacySection().serialize(colorize(text));
    }
}
