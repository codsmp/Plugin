package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.projectiles.ProjectileSource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class PvpRulesListener implements Listener {
    private static final long COMBAT_TAG_DURATION_MS = 15_000L;

    private final Map<UUID, CombatTag> combatTags = new HashMap<>();
    private final Map<UUID, Long> warningCooldowns = new HashMap<>();

    public PvpRulesListener(JavaPlugin plugin) {
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCombatDamage(EntityDamageByEntityEvent event) {
        Player attacker = this.resolveAttacker(event.getDamager());
        if (attacker == null || !(event.getEntity() instanceof Player victim)) {
            return;
        }

        this.tagCombat(attacker, victim);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (this.isTagged(player.getUniqueId()) && this.isRestockInventory(event.getInventory().getType())) {
            event.setCancelled(true);
            this.warn(player, "No restocking during combat.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (this.isTagged(player.getUniqueId()) && this.isRestockInventory(event.getView().getTopInventory().getType())) {
            event.setCancelled(true);
            this.warn(player, "No restocking during combat.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        if (this.isTagged(player.getUniqueId()) && this.isRestockInventory(event.getView().getTopInventory().getType())) {
            event.setCancelled(true);
            this.warn(player, "No restocking during combat.");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!this.isTagged(player.getUniqueId())) {
            return;
        }

        this.clearPair(player.getUniqueId());
        player.setHealth(0.0D);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        this.clearPair(event.getEntity().getUniqueId());
    }

    private void tagCombat(Player attacker, Player victim) {
        long expiresAt = System.currentTimeMillis() + COMBAT_TAG_DURATION_MS;
        this.combatTags.put(attacker.getUniqueId(), new CombatTag(victim.getUniqueId(), expiresAt));
        this.combatTags.put(victim.getUniqueId(), new CombatTag(attacker.getUniqueId(), expiresAt));

        if (this.isRestockInventory(attacker.getOpenInventory().getTopInventory().getType())) {
            attacker.closeInventory();
        }
        if (this.isRestockInventory(victim.getOpenInventory().getTopInventory().getType())) {
            victim.closeInventory();
        }

        this.warn(attacker, "You are now combat tagged.");
        this.warn(victim, "You are now combat tagged.");
    }

    private boolean isRestockInventory(InventoryType type) {
        return switch (type) {
            case CRAFTING, CREATIVE, WORKBENCH, ANVIL, SMITHING, GRINDSTONE, LOOM, STONECUTTER, ENCHANTING, MERCHANT, CARTOGRAPHY -> false;
            default -> true;
        };
    }

    private Player resolveAttacker(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }

        if (damager instanceof Projectile projectile) {
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player player) {
                return player;
            }
        }

        return null;
    }

    private void warn(Player player, String message) {
        long now = System.currentTimeMillis();
        long last = this.warningCooldowns.getOrDefault(player.getUniqueId(), 0L);
        if (now - last < 1_000L) {
            return;
        }

        this.warningCooldowns.put(player.getUniqueId(), now);
        player.sendMessage(ChatColor.RED + "[PvP Rules] " + ChatColor.WHITE + message);
    }

    private boolean isTagged(UUID playerId) {
        CombatTag tag = this.combatTags.get(playerId);
        if (tag == null) {
            return false;
        }

        if (tag.expiresAtMs() >= System.currentTimeMillis()) {
            return true;
        }

        this.combatTags.remove(playerId);
        return false;
    }

    private void clearPair(UUID playerId) {
        CombatTag tag = this.combatTags.remove(playerId);
        if (tag != null) {
            this.combatTags.remove(tag.opponentId());
        }
    }

    private record CombatTag(UUID opponentId, long expiresAtMs) {
    }
}