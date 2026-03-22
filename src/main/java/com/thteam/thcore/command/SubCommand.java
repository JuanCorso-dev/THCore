package com.thteam.thcore.command;

import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

/**
 * Represents a subcommand under a BaseCommand.
 *
 * Usage:
 *   registerSubCommand(new SubCommand() {
 *       public String getName() { return "reload"; }
 *       public String getPermission() { return "myplugin.reload"; }
 *       public void execute(CommandSender sender, String[] args) { ... }
 *   });
 */
public interface SubCommand {

    /** The literal name matched from args[0] (e.g. "reload", "give", "info"). */
    String getName();

    /**
     * Permission node required to run this subcommand.
     * Return null to allow anyone to run it.
     */
    default String getPermission() {
        return null;
    }

    /** Called when the subcommand is executed by a sender with permission. */
    void execute(CommandSender sender, String[] args);

    /**
     * Tab-complete suggestions for this subcommand (args beyond args[0]).
     * args[0] is already the subcommand name when this is called.
     */
    default List<String> tabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }

    /** Short description shown in /command help listings. */
    default String getDescription() {
        return "";
    }

    /** Usage hint shown next to the subcommand in help listings. */
    default String getUsage() {
        return "";
    }
}
