package com.thteam.thcore.command;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import com.thteam.thcore.message.MessageUtil;
import org.bukkit.command.CommandSender;

import java.util.List;

/**
 * Built-in /thcore command.
 * Subcommands: reload, info, hooks
 */
public class THCoreCommand extends BaseCommand {

    public THCoreCommand(THCore plugin) {
        super(plugin);

        registerSubCommand(new SubCommand() {
            @Override
            public String getName() { return "reload"; }

            @Override
            public String getPermission() { return "thcore.admin"; }

            @Override
            public String getDescription() { return "Recarga la configuración"; }

            @Override
            public void execute(CommandSender sender, String[] args) {
                plugin.getConfigManager().reload();
                plugin.getMessageManager().reload();
                plugin.getMessageManager().sendReloadSuccess(sender);
            }
        });

        registerSubCommand(new SubCommand() {
            @Override
            public String getName() { return "info"; }

            @Override
            public String getPermission() { return "thcore.admin"; }

            @Override
            public String getDescription() { return "Muestra información del core"; }

            @Override
            public void execute(CommandSender sender, String[] args) {
                MessageUtil.send(sender, "<gray>=============================================");
                MessageUtil.send(sender, "  <aqua><bold>THCore</bold></aqua> <gray>v" + plugin.getDescription().getVersion());
                MessageUtil.send(sender, "  <gray>Base de datos: <white>" + plugin.getDatabaseManager().getType());
                MessageUtil.send(sender, "  <gray>Hooks activos: <white>" + plugin.getHookManager().getActiveHookCount()
                    + "<gray>/" + plugin.getHookManager().getAll().size());
                MessageUtil.send(sender, "<gray>=============================================");
            }
        });

        registerSubCommand(new SubCommand() {
            @Override
            public String getName() { return "hooks"; }

            @Override
            public String getPermission() { return "thcore.admin"; }

            @Override
            public String getDescription() { return "Muestra el estado de cada hook"; }

            @Override
            public void execute(CommandSender sender, String[] args) {
                MessageUtil.send(sender, "<gray>--- Hooks ---");
                for (BaseHook hook : plugin.getHookManager().getAll()) {
                    String status = hook.isEnabled()
                        ? "<green>✔ " + hook.getHookName()
                        : "<red>✘ " + hook.getHookName();
                    MessageUtil.send(sender, "  " + status);
                }
            }
        });

        registerSubCommand(new SubCommand() {
            @Override
            public String getName() { return "help"; }

            @Override
            public String getDescription() { return "Muestra esta ayuda"; }

            @Override
            public void execute(CommandSender sender, String[] args) {
                onNoArgs(sender, "thcore");
            }
        });
    }

    @Override
    protected void onNoArgs(CommandSender sender, String label) {
        MessageUtil.send(sender, "<gray>--- <aqua>THCore</aqua> Commands ---");
        for (SubCommand sub : getSubCommands()) {
            if (sub.getPermission() == null || sender.hasPermission(sub.getPermission())) {
                MessageUtil.send(sender, "  <yellow>/" + label + " " + sub.getName()
                    + " <gray>- " + sub.getDescription());
            }
        }
    }
}
