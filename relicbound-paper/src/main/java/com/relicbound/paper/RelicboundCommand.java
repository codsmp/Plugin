package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Level;

public final class RelicboundCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final RelicboundCore core;
    private final PaperSpellEngine spellEngine;

    public RelicboundCommand(JavaPlugin plugin, RelicboundCore core, PaperSpellEngine spellEngine) {
        this.plugin = plugin;
        this.core = core;
        this.spellEngine = spellEngine;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            String commandName = command.getName().toLowerCase(java.util.Locale.ROOT);

            if (commandName.equals("spell") || commandName.equals("witchspells")) {
                return this.openSpellMenu(sender);
            }

            if (commandName.equals("witchupgrade")) {
                if (sender instanceof Player player) {
                    return this.handleUpgrade(player);
                }
                sender.sendMessage("Players only.");
                return true;
            }

            if (commandName.equals("witchgrant")) {
                return this.handleGrant(sender, args);
            }

            if ((commandName.equals("info") || commandName.equals("witch")) && args.length == 0) {
                return this.openInfo(sender);
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

            if ("debug".equalsIgnoreCase(args[0])) {
                return this.debugPlayer(sender, args);
            }

            if ("upgrade".equalsIgnoreCase(args[0])) {
                if (sender instanceof Player player) {
                    return this.handleUpgrade(player);
                }
                sender.sendMessage("Players only.");
                return true;
            }

            if ("grant".equalsIgnoreCase(args[0]) && sender.hasPermission("witch.admin.grant")) {
                String[] grantArgs = java.util.Arrays.copyOfRange(args, 1, args.length);
                return this.handleGrant(sender, grantArgs);
            }

            if (sender instanceof Player player) {
                new RelicMenu(this.core).open(player);
                return true;
            }

            sender.sendMessage("Usage: /info <guide|spells|upgrade|grant|debug>");
            return true;
        } catch (Throwable t) {
            this.plugin.getLogger().log(Level.SEVERE, "Error executing /info command", t);
            sender.sendMessage(ChatColor.RED + "An unexpected error occurred while executing that command. See server logs for details.");
            if (sender.hasPermission("witch.admin.grant") || (sender instanceof org.bukkit.entity.Player p && p.isOp())) {
                sender.sendMessage(ChatColor.RED + t.getClass().getSimpleName() + ": " + (t.getMessage() == null ? "(no message)" : t.getMessage()));
            }
            return true;
        }
    }

    private boolean handleUpgrade(Player player) {
        try {
            PlayerRelicState before = this.core.findPlayerState(player.getUniqueId().toString()).orElse(null);
            PlayerRelicState after = this.core.upgradeTier(player.getUniqueId().toString());
            if (after != null && (before == null || before.tier() != after.tier())) {
                player.sendMessage(ChatColor.GOLD + "[Witch] " + ChatColor.YELLOW + "Your tier advanced to " + ChatColor.WHITE + after.tier().name() + ChatColor.YELLOW + "!");
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

    private boolean handleGrant(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /witch grant <player> <essenceType> <amount>");
            return true;
        }
        if (!sender.hasPermission("witch.admin.grant")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to grant essence.");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Target player must be online.");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
        } catch (NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED + "Amount must be a number.");
            return true;
        }

        PlayerRelicState after = this.core.grantEssence(target.getUniqueId().toString(), args[1], amount);
        sender.sendMessage(ChatColor.GREEN + "Granted essence to " + target.getName() + ".");
        target.sendMessage(ChatColor.AQUA + "You received " + amount + " " + args[1] + " essence.");

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
            target.sendMessage(ChatColor.GOLD + "[Witch] " + ChatColor.YELLOW + "Your tier advanced to " + ChatColor.WHITE + after.tier().name() + ChatColor.YELLOW + "!");
        }
        return true;
    }

    private boolean openRelicMenu(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        new RelicMenu(this.core).open(player);
        return true;
    }

    private boolean openInfo(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }
        player.sendMessage(ChatColor.DARK_AQUA + "[Witch] " + ChatColor.GRAY + "Use " + ChatColor.WHITE + "/spell" + ChatColor.GRAY + " to manage loadout, and " + ChatColor.WHITE + "/info guide" + ChatColor.GRAY + " for the guide.");
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

    private boolean debugPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("witch.admin.debug")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use debug.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /witch debug <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Target player must be online.");
            return true;
        }

        String playerId = target.getUniqueId().toString();
        PlayerRelicState relicState = this.core.findPlayerState(playerId).orElse(null);
        PlayerManaState manaState = this.core.getPlayerManaState(playerId).orElse(null);

        sender.sendMessage(ChatColor.GOLD + "[Witch Debug] " + ChatColor.YELLOW + target.getName());
        if (relicState != null) {
            sender.sendMessage(ChatColor.GRAY + "Tier: " + ChatColor.WHITE + relicState.tier().name());
            sender.sendMessage(ChatColor.GRAY + "Unlocked spells: " + ChatColor.WHITE + relicState.unlockedAbilities().size());
        } else {
            sender.sendMessage(ChatColor.GRAY + "Relic state: " + ChatColor.RED + "none");
        }

        if (manaState != null) {
            sender.sendMessage(ChatColor.GRAY + "Archetype: " + ChatColor.WHITE + manaState.archetype().displayName());
            sender.sendMessage(ChatColor.GRAY + "Mana: " + ChatColor.WHITE + manaState.currentMana() + "/" + manaState.maxMana());
            if (manaState.equippedSpellIds().isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "Equipped: " + ChatColor.DARK_GRAY + "none");
            } else {
                for (int i = 0; i < manaState.equippedSpellIds().size(); i++) {
                    String spellId = manaState.equippedSpellIds().get(i);
                    SpellDefinition spell = this.core.findSpell(spellId).orElse(null);
                    String display = spell == null ? spellId + " (?)" : spell.displayName();
                    long cooldown = spell == null ? 0L : this.spellEngine.remainingCooldownSeconds(target, spell);
                    sender.sendMessage(ChatColor.GRAY + "Slot " + (i + 1) + ": " + ChatColor.WHITE + display
                            + ChatColor.GRAY + " | CD: " + ChatColor.WHITE + (cooldown <= 0 ? "READY" : cooldown + "s"));
                }
            }
        } else {
            sender.sendMessage(ChatColor.GRAY + "Mana state: " + ChatColor.RED + "none");
        }

        return true;
    }
}
