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
        long tStart = System.nanoTime();
        long seed = player.getUniqueId().getMostSignificantBits() ^ player.getUniqueId().getLeastSignificantBits();
        PlayerRelicState state = this.core.getOrCreateStartingState(player.getUniqueId().toString(), seed);
        long tAfterLoad = System.nanoTime();
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
            // Defer resource pack to a couple ticks to avoid spike during immediate join
            Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
                try {
                    player.setResourcePack(resourcePackUrl);
                    String promptMessage = this.plugin.getConfig().getString("resource-pack.prompt-message", "").trim();
                    if (!promptMessage.isEmpty()) {
                        player.sendMessage(ChatColor.AQUA + promptMessage);
                    }
                } catch (IllegalArgumentException exception) {
                    this.plugin.getLogger().warning("Invalid resource-pack URL in config: " + resourcePackUrl);
                }
            }, 2L);
        }

        // Give a starter item appropriate for the saved archetype (if any)
        manaStateOptional.ifPresent(m -> StarterItemUtil.giveStarterItem(player, m.archetype()));

        long tBeforeDeferred = System.nanoTime();
        // Defer visibility and team sync slightly to smooth spikes during join
        Bukkit.getScheduler().runTaskLater(this.plugin, () -> {
            long tDeferredStart = System.nanoTime();
            this.spellEngine.syncShadowVeilVisibility(player);
            long tAfterVeil = System.nanoTime();
            this.teamStore.syncPlayer(player);
            long tAfterTeam = System.nanoTime();
            this.plugin.getLogger().info("JOIN timings (ms): load=" + ((tAfterLoad - tStart)/1_000_000) + ", deferredStartDelay=" + ((tDeferredStart - tBeforeDeferred)/1_000_000) + ", veil=" + ((tAfterVeil - tDeferredStart)/1_000_000) + ", team=" + ((tAfterTeam - tAfterVeil)/1_000_000));
        }, 2L);
    }

    private PlayerRelicState promoteFalthera(PlayerRelicState state) {
        java.util.LinkedHashSet<String> unlocked = new java.util.LinkedHashSet<>(state.unlockedAbilities());
        for (SpellDefinition spell : this.core.allSpells()) {
            unlocked.add(spell.id());
        }
        PlayerRelicState promoted = new PlayerRelicState(
            state.playerId(),
            state.relicId(),
            RelicTier.ASCENSION_5,
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
        // Save player state asynchronously to avoid blocking the main thread
        this.core.findPlayerState(player.getUniqueId().toString()).ifPresent(state -> Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.core.savePlayerState(state)));
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(mana -> Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> this.core.savePlayerManaState(mana)));
    }
}
