package com.thteam.thcore.hook.impl;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.BaseHook;

import java.util.UUID;

public class ExcellentEconomyHook extends BaseHook {

    public ExcellentEconomyHook(THCore plugin) {
        super(plugin);
    }

    @Override
    public String getPluginName() {
        return "CoinsEngine";
    }

    @Override
    protected void load() {
        // CoinsEngine API is optional at compile time in this build setup.
    }

    /**
     * Returns the balance for a player in the given currency.
     */
    public double getBalance(UUID uuid, String currencyId) {
        return 0;
    }

    /**
     * Deposits an amount into the player's account for the given currency.
     */
    public void deposit(UUID uuid, String currencyId, double amount) {
        // No-op without CoinsEngine API in compile-only environment.
    }

    /**
     * Withdraws an amount from the player's account for the given currency.
     */
    public void withdraw(UUID uuid, String currencyId, double amount) {
        // No-op without CoinsEngine API in compile-only environment.
    }

    /**
     * Returns true if a currency with the given ID exists.
     */
    public boolean hasCurrency(String currencyId) {
        return false;
    }

    /** Raw API access for advanced usage. */
    public Object getAPI() {
        return null;
    }
}
