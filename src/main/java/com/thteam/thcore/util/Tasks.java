package com.thteam.thcore.util;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

/**
 * Static helper for the Bukkit scheduler.
 *
 * Usage:
 *   Tasks.delay(plugin, 20, () -> player.sendMessage("1 second later"));
 *   Tasks.repeat(plugin, 0, 20, myRunnable);
 *   Tasks.async(plugin, () -> heavyDatabaseWork());
 *   BukkitTask task = Tasks.asyncRepeat(plugin, 0, 100, runnable);
 *   Tasks.cancel(task);
 */
public final class Tasks {

    private Tasks() {}

    // ---------------------------------------------------------------- Sync

    /**
     * Runs a task on the main thread after a delay.
     *
     * @param plugin  Plugin instance
     * @param delay   Delay in ticks (20 ticks = 1 second)
     * @param task    Task to run
     * @return BukkitTask (can be used to cancel)
     */
    public static BukkitTask delay(Plugin plugin, long delay, Runnable task) {
        return plugin.getServer().getScheduler().runTaskLater(plugin, task, delay);
    }

    /**
     * Runs a task on the main thread immediately on the next tick.
     *
     * @param plugin  Plugin instance
     * @param task    Task to run
     * @return BukkitTask
     */
    public static BukkitTask run(Plugin plugin, Runnable task) {
        return plugin.getServer().getScheduler().runTask(plugin, task);
    }

    /**
     * Runs a repeating task on the main thread.
     *
     * @param plugin  Plugin instance
     * @param delay   Initial delay in ticks before first run
     * @param period  Interval in ticks between runs
     * @param task    Task to run
     * @return BukkitTask (cancel to stop)
     */
    public static BukkitTask repeat(Plugin plugin, long delay, long period, Runnable task) {
        return plugin.getServer().getScheduler().runTaskTimer(plugin, task, delay, period);
    }

    // ---------------------------------------------------------------- Async

    /**
     * Runs a task asynchronously (off the main thread) immediately.
     * Use for I/O, database queries, network calls.
     *
     * @param plugin  Plugin instance
     * @param task    Task to run
     * @return BukkitTask
     */
    public static BukkitTask async(Plugin plugin, Runnable task) {
        return plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
    }

    /**
     * Runs a task asynchronously after a delay.
     *
     * @param plugin  Plugin instance
     * @param delay   Delay in ticks
     * @param task    Task to run
     * @return BukkitTask
     */
    public static BukkitTask asyncDelay(Plugin plugin, long delay, Runnable task) {
        return plugin.getServer().getScheduler().runTaskLaterAsynchronously(plugin, task, delay);
    }

    /**
     * Runs a repeating task asynchronously.
     *
     * @param plugin  Plugin instance
     * @param delay   Initial delay in ticks
     * @param period  Interval in ticks between runs
     * @param task    Task to run
     * @return BukkitTask (cancel to stop)
     */
    public static BukkitTask asyncRepeat(Plugin plugin, long delay, long period, Runnable task) {
        return plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, task, delay, period);
    }

    // ---------------------------------------------------------------- Cancel

    /**
     * Cancels a BukkitTask safely (null-check included).
     *
     * @param task Task to cancel, or null (no-op)
     */
    public static void cancel(BukkitTask task) {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }
}
