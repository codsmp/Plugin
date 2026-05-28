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
    private static final int MAX_PROTECTION_LEVEL = 3;
    private static final int MAX_MACES = 1;
    private static final int MAX_MACE_DENSITY_LEVEL = 1;
    private static final int MAX_MACE_WIND_BURST_LEVEL = 1;

    private final Map<UUID, CombatTag> combatTags = new HashMap<>();
    private final Map<UUID, Long> warningCooldowns = new HashMap<>();
    private final JavaPlugin plugin;
    private final NamespacedKey spellStrengthBypassKey;

    public PvpRulesListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.spellStrengthBypassKey = new NamespacedKey(plugin, "spell_strength_bypass");
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
        // Combat logging/tagging removed for Season 3.
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
        this.clearPair(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDeath(PlayerDeathEvent event) {
        this.clearPair(event.getEntity().getUniqueId());
    }

    private void tagCombat(Player attacker, Player victim) {
        // Combat tagging removed.
    }

    private boolean isRestockInventory(InventoryType type) {
        // Restock checks disabled: treat all inventories as non-restockable to avoid blocking
        return false;
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
        return false;
    }

    private void clearPair(UUID playerId) {
        this.combatTags.remove(playerId);
    }

    private void broadcastCombatCountdowns() {
        // Combat countdown removed.
    }

    private void enforcePvpLimits(Player player, boolean notify) {
        if (player == null || !player.isOnline()) {
            return;
        }

        PlayerInventory inventory = player.getInventory();
        boolean changed = false;

        changed |= this.capEnchantmentLevel(inventory.getContents(), Enchantment.PROTECTION, MAX_PROTECTION_LEVEL);
        changed |= this.capEnchantmentLevel(inventory.getArmorContents(), Enchantment.PROTECTION, MAX_PROTECTION_LEVEL);

        changed |= this.capMaterialTotal(inventory, Material.MACE, MAX_MACES);

        // Ensure mace enchants stay within the PvP limits
        for (ItemStack item : inventory.getContents()) {
            if (item == null || item.getType() != Material.MACE) continue;
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            for (Map.Entry<Enchantment, Integer> e : meta.getEnchants().entrySet()) {
                Enchantment ench = e.getKey();
                try {
                    NamespacedKey key = ench.getKey();
                    if (key != null && "density".equalsIgnoreCase(key.getKey())) {
                        int level = e.getValue();
                        if (level > MAX_MACE_DENSITY_LEVEL) {
                            meta.removeEnchant(ench);
                            meta.addEnchant(ench, MAX_MACE_DENSITY_LEVEL, true);
                            item.setItemMeta(meta);
                            changed = true;
                        }
                    } else if (key != null && "wind_burst".equalsIgnoreCase(key.getKey())) {
                        int level = e.getValue();
                        if (level > MAX_MACE_WIND_BURST_LEVEL) {
                            meta.removeEnchant(ench);
                            meta.addEnchant(ench, MAX_MACE_WIND_BURST_LEVEL, true);
                            item.setItemMeta(meta);
                            changed = true;
                        }
                    }
                } catch (NoSuchMethodError ignored) {
                    // Some older server APIs may not support Enchantment#getKey(); ignore in that case
                }
            }
        }

        if (changed && notify) {
            this.warn(player, "PvP limitations applied: enchant caps and max 1 mace.");
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

}