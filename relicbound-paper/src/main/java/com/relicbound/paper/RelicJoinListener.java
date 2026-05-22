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

        // Give a starter item appropriate for the saved archetype (if any)
        if (manaStateOptional.isPresent()) {
            giveStarterForArchetype(player, manaStateOptional.get().archetype());
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        // Give the appropriate starter item for their archetype after respawn
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(m -> giveStarterForArchetype(player, m.archetype()));
    }

    private void giveStarterForArchetype(Player player, PlayerArchetype archetype) {
        String itemName = ChatColor.GOLD + archetype.displayName();
        boolean hasItem = false;
        for (ItemStack it : player.getInventory().getContents()) {
            if (it == null) continue;
            ItemMeta m = it.getItemMeta();
            if (m != null && m.hasDisplayName() && itemName.equals(m.getDisplayName())) {
                hasItem = true;
                break;
            }
        }

        if (!hasItem) {
            Material material = archetype == PlayerArchetype.WAND ? Material.STICK : Material.BLAZE_ROD;
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(itemName);
                java.util.List<String> lore = new java.util.ArrayList<>();
                lore.add(ChatColor.AQUA + "A simple " + archetype.displayName() + " to channel your relic.");
                lore.add(ChatColor.GRAY + "Right-click to cast spells.");
                meta.setLore(lore);
                item.setItemMeta(meta);
            }

            if (player.getInventory().firstEmpty() != -1) {
                player.getInventory().addItem(item);
            } else {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
            }
            player.sendMessage(ChatColor.AQUA + "You received a starter " + archetype.displayName() + ".");
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        this.core.findPlayerState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerState);
        this.core.getPlayerManaState(player.getUniqueId().toString()).ifPresent(this.core::savePlayerManaState);
    }
}
