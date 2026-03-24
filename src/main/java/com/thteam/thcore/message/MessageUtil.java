package com.thteam.thcore.message;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Matches &#RRGGBB and &x&R&R&G&G&B&B hex formats, plus &0-9a-fk-or
    private static final Pattern LEGACY_COLOR = Pattern.compile(
        "&#([0-9a-fA-F]{6})|&x(&[0-9a-fA-F]){6}|&([0-9a-fA-Fk-orK-OR])");

    private static final String[] MINI_COLORS = {
        "black","dark_blue","dark_green","dark_aqua","dark_red","dark_purple",
        "gold","gray","dark_gray","blue","green","aqua","red","light_purple","yellow","white"
    };

    private MessageUtil() {}

    /**
     * Converts legacy &-codes to their MiniMessage equivalents so that both
     * formats can coexist in the same string without escaping issues.
     */
    private static String legacyToMini(String text) {
        Matcher m = LEGACY_COLOR.matcher(text);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (m.group(1) != null) {
                // &#RRGGBB
                m.appendReplacement(sb, "<#" + m.group(1) + ">");
            } else if (m.group(2) != null) {
                // &x&R&R&G&G&B&B — extract the 6 hex digits
                String digits = m.group(0).replaceAll("&", "").substring(1); // strip leading 'x'
                m.appendReplacement(sb, "<#" + digits + ">");
            } else {
                String code = m.group(3).toLowerCase();
                switch (code) {
                    case "l" -> m.appendReplacement(sb, "<bold>");
                    case "o" -> m.appendReplacement(sb, "<italic>");
                    case "n" -> m.appendReplacement(sb, "<underlined>");
                    case "m" -> m.appendReplacement(sb, "<strikethrough>");
                    case "k" -> m.appendReplacement(sb, "<obfuscated>");
                    case "r" -> m.appendReplacement(sb, "<reset>");
                    default -> {
                        int idx = "0123456789abcdef".indexOf(code.charAt(0));
                        if (idx >= 0) m.appendReplacement(sb, "<" + MINI_COLORS[idx] + ">");
                        else m.appendReplacement(sb, m.group(0));
                    }
                }
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * Converts a string with MiniMessage tags and/or legacy &-codes into a Component.
     * Both formats can be mixed freely: "&cHello <gold>world</gold>".
     */
    public static Component colorize(String text) {
        if (text == null || text.isEmpty()) return Component.empty();

        // Normalize section symbol to & then convert all legacy codes to MiniMessage tags.
        // This lets MINI.deserialize handle everything in one pass without escaping issues.
        String normalized = legacyToMini(text.replace("§", "&"));
        return MINI.deserialize(normalized);
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
