package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerArchetype;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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

        // Give a starter wand if the player doesn't already have one
        giveStarterWandIfMissing(player);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Give a starter wand after respawn if they don't have one
        giveStarterWandIfMissing(player);
    }

    private void giveStarterWandIfMissing(Player player) {
        String wandName = ChatColor.GOLD + PlayerArchetype.WAND.displayName();
        boolean hasWand = false;
        for (ItemStack it : player.getInventory().getContents()) {
            if (it == null) continue;
            ItemMeta m = it.getItemMeta();
            if (m != null && m.hasDisplayName() && wandName.equals(m.getDisplayName())) {
                hasWand = true;
                break;
            }
        }

        if (!hasWand) {
            ItemStack wand = new ItemStack(Material.STICK);
            ItemMeta meta = wand.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(wandName);
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.AQUA + "A simple wand to channel your relic.");
                lore.add(ChatColor.GRAY + "Right-click to cast spells.");
                meta.setLore(lore);
                wand.setItemMeta(meta);
            }

            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(wand);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), wand);
            }
            player.sendMessage(ChatColor.AQUA + "You received a starter wand.");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.core.findPlayerState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerState);
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerManaState);
    }
}
