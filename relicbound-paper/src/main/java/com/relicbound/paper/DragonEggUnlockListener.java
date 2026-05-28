package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class DragonEggUnlockListener implements Listener {
    private static final String DRAGON_EGG_SPELL_ID = "bliss_egg";

    private final JavaPlugin plugin;
    private final RelicboundCore core;

    public DragonEggUnlockListener(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        this.tryUnlock(event.getPlayer());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        if (event.getItem().getItemStack().getType() != Material.DRAGON_EGG) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.tryUnlock(player), 1L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> this.tryUnlock(player), 1L);
    }

    private void tryUnlock(Player player) {
        if (!player.isOnline()) {
            return;
        }
        if (!player.getInventory().contains(Material.DRAGON_EGG)) {
            return;
        }

        String playerId = player.getUniqueId().toString();
        boolean alreadyUnlocked = this.core.findPlayerState(playerId)
            .map(state -> state.unlockedAbilities().contains(DRAGON_EGG_SPELL_ID))
            .orElse(false);
        if (!alreadyUnlocked) {
            this.core.learnSpell(playerId, DRAGON_EGG_SPELL_ID);
            player.sendMessage(org.bukkit.ChatColor.DARK_AQUA + "[Witch] " + org.bukkit.ChatColor.WHITE + "Dragon Egg attunement unlocked: Bliss Severance.");
        }

        PlayerManaState mana = this.core.getPlayerManaState(playerId).orElse(null);
        if (mana != null && !mana.equippedSpellIds().contains(DRAGON_EGG_SPELL_ID) && mana.equippedSpellIds().size() < 2) {
            this.core.equipSpell(playerId, DRAGON_EGG_SPELL_ID);
        }
    }
}
