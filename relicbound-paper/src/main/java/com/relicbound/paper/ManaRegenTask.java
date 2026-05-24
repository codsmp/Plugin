package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public final class ManaRegenTask {
    private static final String AQUATIC_BLESSING_ID = "aquatic_blessing";
    private final JavaPlugin plugin;
    private final RelicboundCore core;

    public ManaRegenTask(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    public void startRegenTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ManaRegenTask.this.updatePlayerManaRegen(player, currentTimeMillis);
                }
            }
        }.runTaskTimer(this.plugin, 0L, 1L);
    }

    private void updatePlayerManaRegen(Player player, long currentTimeMillis) {
        Optional<PlayerManaState> manaStateOptional = this.core.getPlayerManaState(player.getUniqueId().toString());
        if (manaStateOptional.isEmpty()) {
            return;
        }

        PlayerManaState manaState = manaStateOptional.get();
        PlayerManaState updated = this.core.updateManaRegen(manaState, currentTimeMillis);

        if (updated.equippedSpellIds().contains(AQUATIC_BLESSING_ID)) {
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.WATER_BREATHING, 100, 0, true, true, true));
            player.addPotionEffect(new org.bukkit.potion.PotionEffect(org.bukkit.potion.PotionEffectType.DOLPHINS_GRACE, 100, 0, true, true, true));
        }

        // Only save if mana actually changed
        if (updated.currentMana() != manaState.currentMana()) {
            this.core.savePlayerManaState(updated);
        }
    }
}
