package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerRelicState;
import org.bukkit.Material;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public final class EssenceGainListener implements Listener {
    private final RelicboundCore core;

    public EssenceGainListener(RelicboundCore core) {
        this.core = core;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Object killer = event.getEntity().getKiller();
        if (killer instanceof Player player) {
            String playerId = player.getUniqueId().toString();
            PlayerRelicState before = this.core.findPlayerState(playerId).orElse(null);
            if (event.getEntity() instanceof Monster) {
                PlayerRelicState after = this.core.grantEssence(playerId, "combat", 8);
                this.announceAutoUpgrade(player, before, after);
            } else if (event.getEntity() instanceof Player) {
                // PvP gives slightly more essence
                PlayerRelicState after = this.core.grantEssence(playerId, "combat", 12);
                this.announceAutoUpgrade(player, before, after);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();
        if (type.name().endsWith("_ORE")) {
            String playerId = player.getUniqueId().toString();
            PlayerRelicState before = this.core.findPlayerState(playerId).orElse(null);
            PlayerRelicState after = this.core.grantEssence(playerId, "mining", 6);
            this.announceAutoUpgrade(player, before, after);
        }
    }

    private void announceAutoUpgrade(Player player, PlayerRelicState before, PlayerRelicState after) {
        if (after == null) {
            return;
        }
        if (before == null || before.tier() == after.tier()) {
            return;
        }
        player.sendMessage(org.bukkit.ChatColor.GOLD + "[Relicbound] " + org.bukkit.ChatColor.YELLOW + "Your relic automatically advanced to " + org.bukkit.ChatColor.WHITE + after.tier().name() + org.bukkit.ChatColor.YELLOW + "!");
    }
}
