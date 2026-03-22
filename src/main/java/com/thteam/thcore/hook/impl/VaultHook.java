package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.chat.Chat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHook extends BaseHook {

    private Economy economy;
    private Permission permissions;
    private Chat chat;

    public VaultHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "Vault";
    }

    @Override
    protected void load() throws Exception {
        RegisteredServiceProvider<Economy> ecoRsp =
            Bukkit.getServicesManager().getRegistration(Economy.class);
        if (ecoRsp != null) economy = ecoRsp.getProvider();

        RegisteredServiceProvider<Permission> permRsp =
            Bukkit.getServicesManager().getRegistration(Permission.class);
        if (permRsp != null) permissions = permRsp.getProvider();

        RegisteredServiceProvider<Chat> chatRsp =
            Bukkit.getServicesManager().getRegistration(Chat.class);
        if (chatRsp != null) chat = chatRsp.getProvider();

        if (economy == null)
            throw new Exception("No economy provider registered with Vault");
    }

    // ------------------------------------------------ Economy

    public boolean hasEconomy() {
        return economy != null;
    }

    public double getBalance(Player player) {
        return economy.getBalance(player);
    }

    public boolean deposit(Player player, double amount) {
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public boolean withdraw(Player player, double amount) {
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public boolean has(Player player, double amount) {
        return economy.has(player, amount);
    }

    public String format(double amount) {
        return economy.format(amount);
    }

    // ------------------------------------------------ Permissions

    public boolean hasPermission(Player player, String node) {
        return permissions != null && permissions.has(player, node);
    }

    // ------------------------------------------------ Raw providers

    public Economy getEconomy() {
        return economy;
    }

    public Permission getPermissions() {
        return permissions;
    }

    public Chat getChat() {
        return chat;
    }
}
