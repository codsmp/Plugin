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
        if (event.getEntity().getKiller() instanceof Player player && event.getEntity() instanceof Monster) {
            this.core.grantEssence(player.getUniqueId().toString(), "combat", 8);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Material type = event.getBlock().getType();
        if (type.name().endsWith("_ORE") || type == Material.ANCIENT_DEBRIS) {
            this.core.grantEssence(player.getUniqueId().toString(), "mining", 6);
        }
    }
}
