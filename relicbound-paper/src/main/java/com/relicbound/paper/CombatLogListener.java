package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class CombatLogListener implements Listener {
    private static final long COMBAT_TAG_MILLIS = 15_000L;
    private static final long FORCED_DISCONNECT_GRACE_MILLIS = 3_000L;

    private final JavaPlugin plugin;
    private final Map<UUID, Long> combatTaggedUntil = new HashMap<>();
    private final Map<UUID, Long> combatLogoutUntil = new HashMap<>();
    private final Map<UUID, Long> forcedDisconnectUntil = new HashMap<>();

    public CombatLogListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player victim = event.getEntity() instanceof Player player ? player : null;
        Player attacker = this.resolveAttacker(event.getDamager());
        if (victim == null || attacker == null) {
            return;
        }

        long until = System.currentTimeMillis() + COMBAT_TAG_MILLIS;
        this.combatTaggedUntil.put(victim.getUniqueId(), until);
        this.combatTaggedUntil.put(attacker.getUniqueId(), until);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onKick(PlayerKickEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        this.forcedDisconnectUntil.put(playerId, System.currentTimeMillis() + FORCED_DISCONNECT_GRACE_MILLIS);
        this.combatLogoutUntil.remove(playerId);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();

        Long forcedUntil = this.forcedDisconnectUntil.get(playerId);
        if (forcedUntil != null && now < forcedUntil) {
            this.forcedDisconnectUntil.remove(playerId);
            this.combatTaggedUntil.remove(playerId);
            return;
        }

        if (this.isTagged(playerId, now)) {
            this.combatLogoutUntil.put(playerId, now + COMBAT_TAG_MILLIS);
        }

        this.combatTaggedUntil.remove(playerId);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        long now = System.currentTimeMillis();

        Long until = this.combatLogoutUntil.get(playerId);
        if (until == null || now > until) {
            this.combatLogoutUntil.remove(playerId);
            return;
        }

        this.combatLogoutUntil.remove(playerId);
        Bukkit.getScheduler().runTask(this.plugin, () -> this.punishCombatLogger(player));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        UUID playerId = event.getEntity().getUniqueId();
        this.combatTaggedUntil.remove(playerId);
        this.combatLogoutUntil.remove(playerId);
        this.forcedDisconnectUntil.remove(playerId);
    }

    private void punishCombatLogger(Player player) {
        if (!player.isOnline()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        List<ItemStack> drops = new ArrayList<>();
        this.collectDrops(drops, inventory.getContents());
        this.collectDrops(drops, inventory.getArmorContents());
        ItemStack offhand = inventory.getItemInOffHand();
        if (this.isDroppable(offhand)) {
            drops.add(offhand.clone());
        }

        World world = player.getWorld();
        Location location = player.getLocation();
        for (ItemStack item : drops) {
            world.dropItemNaturally(location, item);
        }

        inventory.clear();
        inventory.setArmorContents(new ItemStack[4]);
        inventory.setItemInOffHand(new ItemStack(Material.AIR));
        player.setExp(0.0F);
        player.setLevel(0);
        player.setHealth(0.0D);

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            if (player.isOnline()) {
                player.kickPlayer(ChatColor.RED + "Combat logging is not allowed.");
            }
        }, 1L);
    }

    private void collectDrops(List<ItemStack> drops, ItemStack[] items) {
        if (items == null) {
            return;
        }

        for (ItemStack item : items) {
            if (this.isDroppable(item)) {
                drops.add(item.clone());
            }
        }
    }

    private boolean isDroppable(ItemStack item) {
        return item != null && item.getType() != Material.AIR && item.getAmount() > 0;
    }

    private Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    private boolean isTagged(UUID playerId, long now) {
        Long until = this.combatTaggedUntil.get(playerId);
        if (until == null) {
            return false;
        }
        if (now > until) {
            this.combatTaggedUntil.remove(playerId);
            return false;
        }
        return true;
    }
}
