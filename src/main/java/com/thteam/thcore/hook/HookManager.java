package com.thteam.thcore.hook;

import com.thteam.thcore.THCore;
import com.thteam.thcore.hook.impl.*;

import java.util.*;

/**
 * Registers and loads all soft-depend API hooks.
 * Add new hooks by calling register() in the constructor.
 */
public class HookManager {

    private final List<BaseHook> hooks = new ArrayList<>();
    private final Map<Class<? extends BaseHook>, BaseHook> hookIndex = new HashMap<>();

    public HookManager(THCore plugin) {
        register(new VaultHook(plugin));
        register(new PlaceholderAPIHook(plugin));
        register(new WorldGuardHook(plugin));
        register(new NexoHook(plugin));
        register(new OraxenHook(plugin));
        register(new ItemsAdderHook(plugin));
        register(new MythicMobsHook(plugin));
        register(new ExcellentEconomyHook(plugin));
        register(new PlayerPointsHook(plugin));
        register(new LuckPermsHook(plugin));
    }

    private void register(BaseHook hook) {
        hooks.add(hook);
        hookIndex.put(hook.getClass(), hook);
    }

    /** Attempts to load all registered hooks. */
    public void loadAll() {
        hooks.forEach(BaseHook::tryLoad);
    }

    /** Unloads hooks that require explicit cleanup (e.g. PlaceholderAPI). */
    public void unloadAll() {
        for (BaseHook hook : hooks) {
            if (hook instanceof PlaceholderAPIHook papi && papi.isEnabled()) {
                papi.unregister();
            }
        }
    }

    /**
     * Returns the hook instance if it was loaded successfully, or null.
     *
     * Usage: VaultHook vault = hookManager.getHook(VaultHook.class);
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseHook> T getHook(Class<T> type) {
        BaseHook hook = hookIndex.get(type);
        return (hook != null && hook.isEnabled()) ? (T) hook : null;
    }

    /** Returns a list of all registered hooks (enabled or not). */
    public List<BaseHook> getAll() {
        return Collections.unmodifiableList(hooks);
    }

    /** Returns the count of hooks that loaded successfully. */
    public int getActiveHookCount() {
        return (int) hooks.stream().filter(BaseHook::isEnabled).count();
    }
}
