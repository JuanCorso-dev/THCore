package com.thteam.thcore.gui;

import com.thteam.thcore.THCore;
import com.thteam.thcore.api.THCoreAPI;
import com.thteam.thcore.hook.impl.VaultHook;
import com.thteam.thcore.message.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;
import java.util.function.Supplier;

/**
 * Functional interface representing an action executed on a Player.
 *
 * Use the static factory methods for the most common actions.
 * Since this is a @FunctionalInterface, any lambda works too:
 *   GUIAction custom = player -> player.setFlying(true);
 *
 * Combine multiple actions with GUIAction.chain(...).
 */
@FunctionalInterface
public interface GUIAction {

    /** Execute this action for the given player. */
    void execute(Player player);

    // ================================================================
    // Static factories
    // ================================================================

    /**
     * Sends a MiniMessage-formatted message to the player.
     * Supports legacy &-codes and PlaceholderAPI if available.
     */
    static GUIAction message(String text) {
        return player -> {
            String parsed = parsePlaceholders(player, text);
            player.sendMessage(MessageUtil.colorize(parsed));
        };
    }

    /**
     * Broadcasts a MiniMessage-formatted message to all online players.
     */
    static GUIAction broadcast(String text) {
        return player -> {
            String parsed = parsePlaceholders(player, text);
            Bukkit.broadcast(MessageUtil.colorize(parsed));
        };
    }

    /**
     * Runs a command as the console.
     * Use %player% as a placeholder for the player's name.
     */
    static GUIAction console(String command) {
        return player -> {
            String cmd = command.replace("%player%", player.getName());
            cmd = parsePlaceholders(player, cmd);
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        };
    }

    /**
     * Runs a command as the player (as if they typed it in chat).
     * Use %player% as a placeholder for the player's name.
     */
    static GUIAction playerCommand(String command) {
        return player -> {
            String cmd = command.replace("%player%", player.getName());
            cmd = parsePlaceholders(player, cmd);
            // Strip leading slash if present
            if (cmd.startsWith("/")) cmd = cmd.substring(1);
            player.performCommand(cmd);
        };
    }

    /**
     * Closes the player's currently open inventory.
     */
    static GUIAction close() {
        return Player::closeInventory;
    }

    /**
     * Plays a sound at the player's location.
     */
    static GUIAction sound(Sound sound, float volume, float pitch) {
        return player -> player.playSound(player.getLocation(), sound, volume, pitch);
    }

    /**
     * Sends a title + subtitle to the player.
     *
     * @param titleText    MiniMessage title text
     * @param subtitleText MiniMessage subtitle text
     * @param fadeIn       Fade-in ticks
     * @param stay         Stay ticks
     * @param fadeOut      Fade-out ticks
     */
    static GUIAction title(String titleText, String subtitleText, int fadeIn, int stay, int fadeOut) {
        return player -> {
            Component t  = MessageUtil.colorize(parsePlaceholders(player, titleText));
            Component st = MessageUtil.colorize(parsePlaceholders(player, subtitleText));
            player.showTitle(Title.title(t, st, Title.Times.times(
                Duration.ofMillis(fadeIn  * 50L),
                Duration.ofMillis(stay    * 50L),
                Duration.ofMillis(fadeOut * 50L)
            )));
        };
    }

    /**
     * Gives money to the player via Vault.
     * Does nothing if Vault is not installed.
     */
    static GUIAction giveMoney(double amount) {
        return player -> {
            VaultHook vault = THCoreAPI.getVault();
            if (vault != null) vault.deposit(player, amount);
        };
    }

    /**
     * Takes money from the player via Vault.
     * Does nothing if Vault is not installed or player doesn't have enough.
     */
    static GUIAction takeMoney(double amount) {
        return player -> {
            VaultHook vault = THCoreAPI.getVault();
            if (vault != null && vault.has(player, amount)) {
                vault.withdraw(player, amount);
            }
        };
    }

    /**
     * Opens a BaseGUI (chest-only) for the player.
     * The factory is called lazily on click — use a lambda: () -> new MyMenu(plugin)
     */
    static GUIAction openMenu(Supplier<BaseGUI> menuFactory) {
        return player -> {
            BaseGUI menu = menuFactory.get();
            menu.open(player);
        };
    }

    /**
     * Opens a FullGUI (full inventory) for the player.
     */
    static GUIAction openFullMenu(Supplier<FullGUI> menuFactory) {
        return player -> {
            FullGUI menu = menuFactory.get();
            menu.open(player);
        };
    }

    /**
     * Executes multiple actions in order.
     * Equivalent to action1.andThen(action2).andThen(action3)...
     */
    static GUIAction chain(GUIAction... actions) {
        return player -> {
            for (GUIAction action : actions) {
                action.execute(player);
            }
        };
    }

    /**
     * Returns a new GUIAction that runs this action followed by the other.
     */
    default GUIAction andThen(GUIAction other) {
        return player -> {
            this.execute(player);
            other.execute(player);
        };
    }

    // ================================================================
    // Internal helper
    // ================================================================

    private static String parsePlaceholders(Player player, String text) {
        var papi = THCoreAPI.getPAPI();
        if (papi != null) {
            return papi.parse(player, text);
        }
        return text;
    }
}
