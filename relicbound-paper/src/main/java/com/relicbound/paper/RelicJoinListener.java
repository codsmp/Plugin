package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class RelicJoinListener implements Listener {
    private final JavaPlugin plugin;
    private final RelicboundCore core;
    private final ArchetypeSelectionMenu archetypeSelectionMenu;

    public RelicJoinListener(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
        this.archetypeSelectionMenu = new ArchetypeSelectionMenu(core);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        long seed = player.getUniqueId().getMostSignificantBits() ^ player.getUniqueId().getLeastSignificantBits();
        PlayerRelicState state = this.core.getOrCreateStartingState(player.getUniqueId().toString(), seed);
        player.sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "Your relic awakens: " + ChatColor.WHITE + state.relicId());

        // Initialize or retrieve mana state
        java.util.Optional<com.relicbound.core.model.PlayerManaState> manaStateOptional = this.core.getPlayerManaState(player.getUniqueId().toString());
        if (manaStateOptional.isEmpty()) {
            // First join - show archetype selection
            this.archetypeSelectionMenu.open(player);
            player.sendMessage(ChatColor.AQUA + "Choose your path - Wand or Staff!");
        } else {
            PlayerManaState manaState = manaStateOptional.get();
            player.sendMessage(ChatColor.AQUA + "[Relicbound] " + ChatColor.WHITE + "Welcome back, " + manaState.archetype().displayName() + "!");
        }

        if (manaStateOptional.isEmpty()) {
            StarterItemUtil.findHeldStarterArchetype(player).ifPresent(archetype -> this.core.getOrCreatePlayerManaState(player.getUniqueId().toString(), archetype));
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
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(m -> Bukkit.getScheduler().runTaskLater(this.plugin, () -> StarterItemUtil.giveStarterItem(player, m.archetype()), 1L));
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(m -> {
            if (!event.getKeepInventory()) {
                return;
            }

            if (removeOneStarterItem(player, m.archetype())) {
                event.getDrops().add(StarterItemUtil.createStarterItem(m.archetype()));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.core.findPlayerState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerState);
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerManaState);
    }

    private boolean removeOneStarterItem(Player player, PlayerArchetype archetype) {
        String displayName = org.bukkit.ChatColor.GOLD + archetype.displayName();
        org.bukkit.inventory.ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            org.bukkit.inventory.ItemStack item = contents[i];
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasDisplayName()) {
                continue;
            }
            if (displayName.equals(item.getItemMeta().getDisplayName())) {
                contents[i] = null;
                player.getInventory().setContents(contents);
                return true;
            }
        }
        return false;
    }
}
