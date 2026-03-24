package com.thteam.thcore.command;

import com.thteam.thcore.THCore;
import com.thteam.thcore.message.MessageUtil;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Base class for all TH Team plugin commands.
 *
 * Usage in your plugin:
 *
 *   public class MyCommand extends BaseCommand {
 *       public MyCommand(THCore plugin) {
 *           super(plugin);
 *           registerSubCommand(new SubCommand() {
 *               public String getName() { return "reload"; }
 *               public String getPermission() { return "myplugin.reload"; }
 *               public void execute(CommandSender sender, String[] args) {
 *                   sender.sendMessage("Reloaded!");
 *               }
 *           });
 *       }
 *   }
 *
 *   // In your plugin's onEnable():
 *   new MyCommand(plugin).register(this, "mycommand");
 */
public abstract class BaseCommand implements CommandExecutor, TabCompleter {

    protected final THCore plugin;

    // Linked map to preserve registration order (useful for /help listings)
    private final Map<String, SubCommand> subcommands = new LinkedHashMap<>();

    protected BaseCommand(THCore plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers this command with the server.
     * The command name must exist in the owningPlugin's plugin.yml.
     */
    public void register(Plugin owningPlugin, String commandName) {
        if (!(owningPlugin instanceof JavaPlugin javaPlugin)) {
            plugin.getLogger().warning("Cannot register command '" + commandName + "': plugin is not a JavaPlugin.");
            return;
        }
        PluginCommand cmd = javaPlugin.getCommand(commandName);
        if (cmd == null) {
            plugin.getLogger().warning("Command '" + commandName + "' not found in plugin.yml of "
                + owningPlugin.getName() + "!");
            return;
        }
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    /**
     * Registers a subcommand. Call this in your constructor.
     */
    protected void registerSubCommand(SubCommand sub) {
        subcommands.put(sub.getName().toLowerCase(), sub);
    }

    // ------------------------------------------------ CommandExecutor

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            onNoArgs(sender, label);
            return true;
        }

        String subName = args[0].toLowerCase();
        SubCommand sub = subcommands.get(subName);

        if (sub == null) {
            String msg = plugin.getConfigManager().getString(
                "messages.unknown-subcommand",
                "<red>Subcomando desconocido. Usa /<command> help</red>"
            ).replace("<command>", label);
            MessageUtil.send(sender, msg);
            return true;
        }

        if (sub.getPermission() != null && !sender.hasPermission(sub.getPermission())) {
            plugin.getMessageManager().sendNoPermission(sender);
            return true;
        }

        sub.execute(sender, args);
        return true;
    }

    // ------------------------------------------------ TabCompleter

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            return subcommands.values().stream()
                .filter(s -> s.getPermission() == null || sender.hasPermission(s.getPermission()))
                .map(SubCommand::getName)
                .filter(name -> name.startsWith(partial))
                .collect(Collectors.toList());
        }

        // Delegate deeper tab-complete to the matched subcommand
        SubCommand sub = subcommands.get(args[0].toLowerCase());
        if (sub != null && (sub.getPermission() == null || sender.hasPermission(sub.getPermission()))) {
            return sub.tabComplete(sender, args);
        }

        return Collections.emptyList();
    }

    // ------------------------------------------------ Override hooks

    /**
     * Called when the command is executed with no arguments.
     * Override to show help or usage. Default sends a hint.
     */
    protected void onNoArgs(CommandSender sender, String label) {
        MessageUtil.send(sender, "<yellow>Usa <white>/" + label + " help</white> para ver los comandos.</yellow>");
    }

    /**
     * Returns an unmodifiable view of all registered subcommands.
     * Useful for building dynamic /help listings.
     */
    public Collection<SubCommand> getSubCommands() {
        return Collections.unmodifiableCollection(subcommands.values());
    }
}
