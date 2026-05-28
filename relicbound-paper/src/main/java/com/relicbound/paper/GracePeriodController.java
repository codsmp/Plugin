package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public final class GracePeriodController implements Listener, CommandExecutor {
    private static final long DEFAULT_DURATION_MS = 60L * 60L * 1000L; // 1 hour

    private final JavaPlugin plugin;
    private volatile long gracePeriodEndsAtMs = 0L;
    private BukkitTask endTask;

    public GracePeriodController(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("relicbound.admin.grace")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length > 0) {
            if ("status".equalsIgnoreCase(args[0])) {
                sender.sendMessage(this.statusMessage());
                return true;
            }
            if ("stop".equalsIgnoreCase(args[0]) || "end".equalsIgnoreCase(args[0])) {
                if (!this.isGraceActive()) {
                    sender.sendMessage(ChatColor.YELLOW + "Grace period is not active.");
                    return true;
                }
                this.stopGracePeriod("Grace period was ended by " + sender.getName() + ".");
                return true;
            }
        }

        if (this.isGraceActive()) {
            sender.sendMessage(this.statusMessage());
            return true;
        }

        this.startGracePeriod(DEFAULT_DURATION_MS, sender.getName());
        return true;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (!this.isGraceActive()) {
            return;
        }

        event.setCancelled(true);
    }

    private void startGracePeriod(long durationMs, String actorName) {
        long now = System.currentTimeMillis();
        this.gracePeriodEndsAtMs = now + Math.max(1000L, durationMs);

        if (this.endTask != null) {
            this.endTask.cancel();
        }

        long ticks = Math.max(1L, (this.gracePeriodEndsAtMs - now + 49L) / 50L);
        this.endTask = Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.stopGracePeriod("Grace period has ended."), ticks);
        Bukkit.getLogger().info("[PvP Rules] Grace period started by " + actorName + ". No one can take damage for 1 hour.");
    }

    private void stopGracePeriod(String message) {
        this.gracePeriodEndsAtMs = 0L;
        if (this.endTask != null) {
            this.endTask.cancel();
            this.endTask = null;
        }
        Bukkit.getLogger().info("[PvP Rules] " + message);
    }

    private boolean isGraceActive() {
        if (this.gracePeriodEndsAtMs <= 0L) {
            return false;
        }
        if (this.gracePeriodEndsAtMs > System.currentTimeMillis()) {
            return true;
        }
        this.gracePeriodEndsAtMs = 0L;
        return false;
    }

    private String statusMessage() {
        if (!this.isGraceActive()) {
            return ChatColor.YELLOW + "Grace period is not active.";
        }
        long remainingMs = this.gracePeriodEndsAtMs - System.currentTimeMillis();
        long totalSeconds = Math.max(0L, remainingMs / 1000L);
        long minutes = totalSeconds / 60L;
        long seconds = totalSeconds % 60L;
        return ChatColor.GREEN + "Grace period active. Time remaining: " + ChatColor.WHITE + minutes + "m " + seconds + "s";
    }
}
