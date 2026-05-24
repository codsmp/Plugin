package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;

public final class PvpRulesListener implements Listener {
    private static final long COMBAT_TAG_DURATION_MS = 15_000L;
    private static final int MAX_PROTECTION_LEVEL = 3;
    private static final int MAX_XP_BOTTLES = 192;
    private static final int MAX_COBWEBS = 192;
    private static final int MAX_GOD_APPLES = 1;
    private static final int MAX_BREEZE_RODS = 64;

    private final Map<UUID, CombatTag> combatTags = new HashMap<>();
    private final Map<UUID, Long> warningCooldowns = new HashMap<>();
    private final JavaPlugin plugin;
    private final NamespacedKey spellStrengthBypassKey;

    public PvpRulesListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.spellStrengthBypassKey = new NamespacedKey(plugin, "spell_strength_bypass");
        Bukkit.getScheduler().runTaskTimer(this.plugin, this::broadcastCombatCountdowns, 20L, 20L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.enforcePvpLimits(player, true), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.enforcePvpLimits(player, true), 2L);
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

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.enforcePvpLimits(player, false), 1L);
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

        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.enforcePvpLimits(player, false), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.enforcePvpLimits(player, false), 1L);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        PotionEffect newEffect = event.getNewEffect();
        if (newEffect == null || newEffect.getType() != PotionEffectType.STRENGTH) {
            return;
        }
        if (newEffect.getAmplifier() <= 0) {
            return;
        }
        if (player.getPersistentDataContainer().has(this.spellStrengthBypassKey, PersistentDataType.BYTE)) {
            return;
        }

        event.setCancelled(true);
        player.addPotionEffect(new PotionEffect(
            PotionEffectType.STRENGTH,
            newEffect.getDuration(),
            0,
            newEffect.isAmbient(),
            newEffect.hasParticles(),
            newEffect.hasIcon()
        ));
        this.warn(player, "Strength II is limited. Applied Strength I instead.");
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

    private void broadcastCombatCountdowns() {
        long now = System.currentTimeMillis();
        for (UUID playerId : new ArrayList<>(this.combatTags.keySet())) {
            CombatTag tag = this.combatTags.get(playerId);
            if (tag == null) {
                continue;
            }

            long remainingMs = tag.expiresAtMs() - now;
            Player player = Bukkit.getPlayer(playerId);
            if (remainingMs <= 0L) {
                this.clearPair(playerId);
                if (player != null && player.isOnline()) {
                    player.sendMessage(ChatColor.GREEN + "[PvP Rules] " + ChatColor.WHITE + "You are now out of combat.");
                }
                continue;
            }

            if (player != null && player.isOnline()) {
                long secondsLeft = Math.max(1L, (remainingMs + 999L) / 1000L);
                player.sendMessage(ChatColor.RED + "[PvP Rules] " + ChatColor.WHITE + "Combat tag ends in " + ChatColor.YELLOW + secondsLeft + "s" + ChatColor.WHITE + ".");
            }
        }
    }

    private void enforcePvpLimits(Player player, boolean notify) {
        if (player == null || !player.isOnline()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean changed = false;

        changed |= this.capEnchantmentLevel(inventory.getContents(), Enchantment.PROTECTION, MAX_PROTECTION_LEVEL);
        changed |= this.capEnchantmentLevel(inventory.getArmorContents(), Enchantment.PROTECTION, MAX_PROTECTION_LEVEL);

        changed |= this.capMaterialTotal(inventory, Material.EXPERIENCE_BOTTLE, MAX_XP_BOTTLES);
        changed |= this.capMaterialTotal(inventory, Material.COBWEB, MAX_COBWEBS);
        changed |= this.capMaterialTotal(inventory, Material.ENCHANTED_GOLDEN_APPLE, MAX_GOD_APPLES);
        changed |= this.capMaterialTotal(inventory, Material.BREEZE_ROD, MAX_BREEZE_RODS);

        PotionEffect strength = player.getPotionEffect(PotionEffectType.STRENGTH);
        if (strength != null && strength.getAmplifier() > 0) {
            player.removePotionEffect(PotionEffectType.STRENGTH);
            player.addPotionEffect(new PotionEffect(
                PotionEffectType.STRENGTH,
                strength.getDuration(),
                0,
                strength.isAmbient(),
                strength.hasParticles(),
                strength.hasIcon()
            ));
            changed = true;
        }

        if (changed && notify) {
            this.warn(player, "PvP limitations applied: Prot III, Strength I, XP/Web caps, 1 god apple, 1 stack breeze rods.");
        }
    }

    private boolean capEnchantmentLevel(ItemStack[] items, Enchantment enchantment, int maxLevel) {
        boolean changed = false;
        if (items == null) {
            return false;
        }
        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }
            ItemMeta meta = item.getItemMeta();
            if (meta == null) {
                continue;
            }
            Integer currentLevel = meta.getEnchants().get(enchantment);
            if (currentLevel == null || currentLevel <= maxLevel) {
                continue;
            }
            meta.removeEnchant(enchantment);
            meta.addEnchant(enchantment, maxLevel, true);
            item.setItemMeta(meta);
            changed = true;
        }
        return changed;
    }

    private boolean capMaterialTotal(PlayerInventory inventory, Material material, int maxTotal) {
        ItemStack[] contents = inventory.getContents();
        int total = 0;
        for (ItemStack item : contents) {
            if (item != null && item.getType() == material) {
                total += item.getAmount();
            }
        }
        if (total <= maxTotal) {
            return false;
        }

        int overflow = total - maxTotal;
        boolean changed = false;
        for (int i = contents.length - 1; i >= 0 && overflow > 0; i--) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != material) {
                continue;
            }
            int amount = item.getAmount();
            if (amount <= overflow) {
                overflow -= amount;
                contents[i] = null;
            } else {
                item.setAmount(amount - overflow);
                overflow = 0;
            }
            changed = true;
        }

        if (changed) {
            inventory.setContents(contents);
        }
        return changed;
    }

    private record CombatTag(UUID opponentId, long expiresAtMs) {
    }
}