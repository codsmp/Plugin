package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Optional;

public final class ManaBarDisplay {
    private final JavaPlugin plugin;
    private final RelicboundCore core;

    public ManaBarDisplay(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    public void startDisplayTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ManaBarDisplay.this.updatePlayerManaBar(player);
                }
            }
        }.runTaskTimer(this.plugin, 0L, 20L);
    }

    private void updatePlayerManaBar(Player player) {
        Optional<PlayerManaState> manaStateOptional = this.core.getPlayerManaState(player.getUniqueId().toString());
        if (manaStateOptional.isEmpty()) {
            return;
        }

        PlayerManaState manaState = manaStateOptional.get();
        manaState = this.core.updateManaRegen(manaState, System.currentTimeMillis());

        int currentMana = manaState.currentMana();
        int maxMana = manaState.maxMana();
        double percentage = (double) currentMana / maxMana * 100.0;

        String bar = this.buildManaBar(currentMana, maxMana);
        String archetype = manaState.archetype().displayName();
        String actionBar = ChatColor.AQUA + "Mana: " + ChatColor.WHITE + bar + ChatColor.GRAY + " [" + currentMana + "/" + maxMana + "]" + ChatColor.RESET;

        player.sendActionBar(actionBar);
    }

    private String buildManaBar(int current, int max) {
        int barLength = 20;
        int filled = Math.round((float) current / max * barLength);
        StringBuilder bar = new StringBuilder();
        bar.append(ChatColor.BLUE);
        for (int i = 0; i < filled; i++) {
            bar.append("▮");
        }
        bar.append(ChatColor.DARK_GRAY);
        for (int i = filled; i < barLength; i++) {
            bar.append("▮");
        }
        return bar.toString();
    }
}
