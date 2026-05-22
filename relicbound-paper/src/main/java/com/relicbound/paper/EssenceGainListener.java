package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
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
            if (event.getEntity() instanceof Monster) {
                this.core.grantEssence(player.getUniqueId().toString(), "combat", 8);
            } else if (event.getEntity() instanceof Player) {
                // PvP gives slightly more essence
                this.core.grantEssence(player.getUniqueId().toString(), "combat", 12);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();
        if (type.name().endsWith("_ORE")) {
            this.core.grantEssence(player.getUniqueId().toString(), "mining", 6);
        }
    }
}
