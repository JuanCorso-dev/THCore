package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import su.nightexpress.coinsengine.api.CoinsEngineAPI;
import su.nightexpress.coinsengine.api.currency.Currency;

import java.util.UUID;

public class ExcellentEconomyHook extends BaseHook {

    private CoinsEngineAPI api;

    public ExcellentEconomyHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "CoinsEngine";
    }

    @Override
    protected void load() throws Exception {
        RegisteredServiceProvider<CoinsEngineAPI> rsp =
            Bukkit.getServicesManager().getRegistration(CoinsEngineAPI.class);
        if (rsp == null)
            throw new Exception("CoinsEngine service not registered");
        api = rsp.getProvider();
    }

    /**
     * Returns the balance for a player in the given currency.
     */
    public double getBalance(UUID uuid, String currencyId) {
        Currency currency = api.getCurrency(currencyId);
        if (currency == null) return 0;
        return api.getBalance(uuid, currency);
    }

    /**
     * Deposits an amount into the player's account for the given currency.
     */
    public void deposit(UUID uuid, String currencyId, double amount) {
        Currency currency = api.getCurrency(currencyId);
        if (currency == null) return;
        api.depositBalance(uuid, currency, amount);
    }

    /**
     * Withdraws an amount from the player's account for the given currency.
     */
    public void withdraw(UUID uuid, String currencyId, double amount) {
        Currency currency = api.getCurrency(currencyId);
        if (currency == null) return;
        api.withdrawBalance(uuid, currency, amount);
    }

    /**
     * Returns true if a currency with the given ID exists.
     */
    public boolean hasCurrency(String currencyId) {
        return api.getCurrency(currencyId) != null;
    }

    /** Raw API access for advanced usage. */
    public CoinsEngineAPI getAPI() {
        return api;
    }
}
