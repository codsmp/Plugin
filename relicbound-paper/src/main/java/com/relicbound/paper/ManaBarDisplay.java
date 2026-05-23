package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Optional;

public final class ManaBarDisplay {
    private final JavaPlugin plugin;
    private final RelicboundCore core;
    private final PaperSpellEngine spellEngine;

    public ManaBarDisplay(JavaPlugin plugin, RelicboundCore core, PaperSpellEngine spellEngine) {
        this.plugin = plugin;
        this.core = core;
        this.spellEngine = spellEngine;
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
        String bar = this.buildManaBar(currentMana, maxMana);
        String actionBar = ChatColor.AQUA + "Mana " + ChatColor.WHITE + bar + ChatColor.GRAY + " [" + currentMana + "/" + maxMana + "]";
        if (StarterItemUtil.findHeldStarterArchetype(player).isPresent()) {
            String cooldownHud = this.buildCooldownHud(player, manaState);
            actionBar = actionBar + ChatColor.DARK_GRAY + "  |  " + cooldownHud;
        }

        actionBar = actionBar + ChatColor.RESET;

        player.sendActionBar(actionBar);
    }

    private String buildCooldownHud(Player player, PlayerManaState manaState) {
        List<String> equipped = manaState.equippedSpellIds();
        String primary = this.cooldownSlotText(player, equipped, 0, ChatColor.GOLD, "RMB");
        String secondary = this.cooldownSlotText(player, equipped, 1, ChatColor.AQUA, "Shift+RMB");
        return primary + ChatColor.GRAY + "  " + secondary;
    }

    private String cooldownSlotText(Player player, List<String> equipped, int slot, ChatColor accent, String keybind) {
        if (equipped.size() <= slot) {
            return ChatColor.DARK_GRAY + keybind + ": --";
        }

        SpellDefinition spell = this.core.findSpell(equipped.get(slot)).orElse(null);
        if (spell == null) {
            return ChatColor.DARK_GRAY + keybind + ": ??";
        }

        long cooldownSeconds = this.spellEngine.remainingCooldownSeconds(player, spell);
        String shortName = this.abbreviateSpellName(spell.displayName(), 12);
        if (cooldownSeconds <= 0) {
            return accent + keybind + ChatColor.GRAY + ": " + ChatColor.GREEN + shortName + " READY";
        }
        return accent + keybind + ChatColor.GRAY + ": " + ChatColor.RED + shortName + " " + ChatColor.YELLOW + cooldownSeconds + "s";
    }

    private String abbreviateSpellName(String name, int maxLength) {
        if (name.length() <= maxLength) {
            return name;
        }
        return name.substring(0, Math.max(1, maxLength - 3)) + "...";
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
