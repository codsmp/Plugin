package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicTier;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class RelicJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final RelicboundCore core;
    private final PaperSpellEngine spellEngine;
    private final PlayerTeamStore teamStore;
    private final ArchetypeSelectionMenu archetypeSelectionMenu;

    public RelicJoinListener(JavaPlugin plugin, RelicboundCore core, PaperSpellEngine spellEngine, PlayerTeamStore teamStore) {
        this.plugin = plugin;
        this.core = core;
        this.spellEngine = spellEngine;
        this.teamStore = teamStore;
        this.archetypeSelectionMenu = new ArchetypeSelectionMenu(core);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long seed = player.getUniqueId().getMostSignificantBits() ^ player.getUniqueId().getLeastSignificantBits();
        PlayerRelicState state = this.core.getOrCreateStartingState(player.getUniqueId().toString(), seed);
        if (player.getName().equalsIgnoreCase("Falthera") || player.getName().equalsIgnoreCase("braxsmashedyou") || player.getName().equalsIgnoreCase("Aishi___") || player.getName().equalsIgnoreCase("Abbas14") || player.getName().equalsIgnoreCase("lovely_lyla") || player.getName().equalsIgnoreCase("Vyxen123")) {
            state = this.promoteFalthera(state);
        }
        player.sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "Your relic awakens: " + ChatColor.WHITE + state.relicId());

        if (state.pendingRewardSelection()) {
            this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> {
                if (!player.isOnline()) {
                    return;
                }
                PlayerRelicState refreshed = this.core.findPlayerState(player.getUniqueId().toString()).orElse(null);
                if (refreshed == null) {
                    return;
                }
                boolean hasLockedSpells = this.core.allSpells().stream().anyMatch(spell -> !refreshed.unlockedAbilities().contains(spell.id()));
                if (refreshed.pendingRewardSelection() && hasLockedSpells) {
                    this.core.savePlayerState(refreshed.withPendingRewardSelection(true));
                    new SpellMenu(this.plugin, this.core).open(player, SpellMenuMode.REWARD);
                } else if (!hasLockedSpells) {
                    this.core.savePlayerState(refreshed.withPendingRewardSelection(false));
                }
            }, 1L);
        }

        // Initialize or retrieve mana state
        java.util.Optional<com.relicbound.core.model.PlayerManaState> manaStateOptional = this.core.getPlayerManaState(player.getUniqueId().toString());
        if (manaStateOptional.isEmpty()) {
            // First join - show archetype selection
            this.archetypeSelectionMenu.open(player);
            player.sendMessage(ChatColor.AQUA + "Choose your path - Wand or Staff!");
            player.sendMessage(ChatColor.GRAY + "Type /relicbound guide after you choose for a quick walkthrough.");
        } else {
            PlayerManaState manaState = manaStateOptional.get();
            player.sendMessage(ChatColor.AQUA + "[Relicbound] " + ChatColor.WHITE + "Welcome back, " + manaState.archetype().displayName() + "!");
            player.sendMessage(ChatColor.GRAY + "Need a refresher? Use /relicbound guide.");
        }

        if (manaStateOptional.isEmpty()) {
            StarterItemUtil.findAnyStarterArchetype(player).ifPresent(archetype -> this.core.getOrCreatePlayerManaState(player.getUniqueId().toString(), archetype));
        }

        String resourcePackUrl = this.plugin.getConfig().getString("resource-pack.url", "").trim();
        if (!resourcePackUrl.isEmpty()) {
            try {
                player.setResourcePack(resourcePackUrl);
                String promptMessage = this.plugin.getConfig().getString("resource-pack.prompt-message", "").trim();
                if (!promptMessage.isEmpty()) {
                    player.sendMessage(ChatColor.AQUA + promptMessage);
                }
            } catch (IllegalArgumentException exception) {
                this.plugin.getLogger().warning("Invalid resource-pack URL in config: " + resourcePackUrl);
            }
        }

        // Give a starter item appropriate for the saved archetype (if any)
        manaStateOptional.ifPresent(m -> StarterItemUtil.giveStarterItem(player, m.archetype()));

        this.spellEngine.syncShadowVeilVisibility(player);
        this.teamStore.syncPlayer(player);
    }

    private PlayerRelicState promoteFalthera(PlayerRelicState state) {
        java.util.LinkedHashSet<String> unlocked = new java.util.LinkedHashSet<>(state.unlockedAbilities());
        for (SpellDefinition spell : this.core.allSpells()) {
            unlocked.add(spell.id());
        }
        PlayerRelicState promoted = new PlayerRelicState(
            state.playerId(),
            state.relicId(),
            RelicTier.ASCENSION,
            state.currentEssence(),
            state.essenceByType(),
            java.util.List.copyOf(unlocked),
            false
        );
        return this.core.savePlayerState(promoted);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(m -> Bukkit.getScheduler().runTaskLater(this.plugin, () -> StarterItemUtil.giveStarterItem(player, m.archetype()), 1L));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (this.plugin instanceof RelicboundPaperPlugin relicboundPlugin && relicboundPlugin.isResetInProgress()) {
            return;
        }
        Player player = event.getPlayer();
        this.core.findPlayerState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerState);
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerManaState);
    }
}
