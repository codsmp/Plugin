package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerRelicState;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.logging.Level;

public final class RelicboundCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final RelicboundCore core;

    public RelicboundCommand(JavaPlugin plugin, RelicboundCore core) {
        this.plugin = plugin;
        this.core = core;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (command.getName().equalsIgnoreCase("relicboundspells")) {
                return this.openSpellMenu(sender);
            }

            if (command.getName().equalsIgnoreCase("relicbound") && args.length == 0) {
                return this.openRelicMenu(sender);
            }

            if (args.length == 0) {
                return this.openRelicMenu(sender);
            }

            if ("help".equalsIgnoreCase(args[0]) || "guide".equalsIgnoreCase(args[0])) {
                return this.openGuide(sender);
            }

            if ("spells".equalsIgnoreCase(args[0])) {
                return this.openSpellMenu(sender);
            }

            if ("upgrade".equalsIgnoreCase(args[0])) {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("Players only.");
                    return true;
                }
                try {
                    PlayerRelicState before = this.core.findPlayerState(player.getUniqueId().toString()).orElse(null);
                    PlayerRelicState after = this.core.upgradeTier(player.getUniqueId().toString());
                    if (after != null && (before == null || before.tier() != after.tier())) {
                        player.sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "Your relic advanced to " + ChatColor.WHITE + after.tier().name() + ChatColor.YELLOW + "!");
                    } else {
                        player.sendMessage(ChatColor.GREEN + "Your relic has advanced.");
                    }
                    boolean hasLockedSpells = this.core.allSpells().stream().anyMatch(spell -> !after.unlockedAbilities().contains(spell.id()));
                    if (hasLockedSpells) {
                        this.core.savePlayerState(after.withPendingRewardSelection(true));
                        new SpellMenu(this.plugin, this.core).open(player, SpellMenuMode.REWARD);
                    } else {
                        this.core.savePlayerState(after.withPendingRewardSelection(false));
                        new RelicMenu(this.core).open(player);
                    }
                } catch (IllegalStateException exception) {
                    player.sendMessage(ChatColor.RED + exception.getMessage());
                }
                return true;
            }

            if ("grant".equalsIgnoreCase(args[0]) && sender.hasPermission("relicbound.admin.grant")) {
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /relicbound grant <player> <essenceType> <amount>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(ChatColor.RED + "Target player must be online.");
                    return true;
                }
                int amount;
                try {
                    amount = Integer.parseInt(args[3]);
                } catch (NumberFormatException exception) {
                    sender.sendMessage(ChatColor.RED + "Amount must be a number.");
                    return true;
                }
                PlayerRelicState after = this.core.grantEssence(target.getUniqueId().toString(), args[2], amount);
                sender.sendMessage(ChatColor.GREEN + "Granted essence to " + target.getName() + ".");
                target.sendMessage(ChatColor.AQUA + "You received " + amount + " " + args[2] + " essence.");
                if (after != null) {
                    boolean hasLockedSpells = this.core.allSpells().stream().anyMatch(spell -> !after.unlockedAbilities().contains(spell.id()));
                    if (hasLockedSpells && after.pendingRewardSelection()) {
                        this.core.savePlayerState(after.withPendingRewardSelection(true));
                        new SpellMenu(this.plugin, this.core).open(target, SpellMenuMode.REWARD);
                    } else if (!hasLockedSpells) {
                        this.core.savePlayerState(after.withPendingRewardSelection(false));
                    }
                }
                if (after != null && after.pendingRewardSelection()) {
                    target.sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "Your relic advanced to " + ChatColor.WHITE + after.tier().name() + ChatColor.YELLOW + "!");
                }
                return true;
            }

            if (sender instanceof Player player) {
                new RelicMenu(this.core).open(player);
                return true;
            }

            sender.sendMessage("Usage: /relicbound <guide|spells|upgrade|grant>");
            return true;
        } catch (Throwable t) {
            // Log the full exception to server logs and send a concise message to the sender
            this.plugin.getLogger().log(Level.SEVERE, "Error executing /relicbound command", t);
            sender.sendMessage(ChatColor.RED + "An unexpected error occurred while executing that command. See server logs for details.");
            if (sender.hasPermission("relicbound.admin.grant") || (sender instanceof org.bukkit.entity.Player p && p.isOp())) {
                sender.sendMessage(ChatColor.RED + t.getClass().getSimpleName() + ": " + (t.getMessage() == null ? "(no message)" : t.getMessage()));
            }
            return true;
        }
    }

    private boolean openRelicMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        new RelicMenu(this.core).open(player);
        return true;
    }

    private boolean openSpellMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        new SpellMenu(this.plugin, this.core).open(player);
        return true;
    }

    private boolean openGuide(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        new GuideMenu(this.plugin).open(player);
        return true;
    }
}
