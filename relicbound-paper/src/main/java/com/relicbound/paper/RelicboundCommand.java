package com.relicbound.paper;

import com.relicbound.core.RelicboundCore;
import com.relicbound.core.model.PlayerManaState;
import com.relicbound.core.model.PlayerRelicState;
import com.relicbound.core.model.RelicTier;
import com.relicbound.core.model.SpellDefinition;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.LinkedHashSet;
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

            if ("debug".equalsIgnoreCase(args[0])) {
                return this.debugPlayer(sender, args);
            }

            if ("op".equalsIgnoreCase(args[0])) {
                return this.grantOpPower(sender, args);
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

            sender.sendMessage("Usage: /relicbound <guide|spells|upgrade|grant|debug|op>");
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

    private boolean debugPlayer(CommandSender sender, String[] args) {
        if (!sender.hasPermission("relicbound.admin.debug")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use debug.");
            return true;
        }
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /relicbound debug <player>");
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

        sender.sendMessage(ChatColor.GOLD + "[Relicbound Debug] " + ChatColor.YELLOW + target.getName());
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

    private boolean grantOpPower(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Players only.");
            return true;
        }
        if (!player.isOp()) {
            player.sendMessage(ChatColor.RED + "Only operators can use this command.");
            return true;
        }

        String targetId = player.getUniqueId().toString();
        PlayerRelicState current = this.core.findPlayerState(targetId).orElseGet(() -> this.core.getOrCreateStartingState(targetId, player.getUniqueId().getMostSignificantBits() ^ player.getUniqueId().getLeastSignificantBits()));
        LinkedHashSet<String> unlocked = new LinkedHashSet<>(current.unlockedAbilities());
        for (SpellDefinition spell : this.core.allSpells()) {
            unlocked.add(spell.id());
        }

        PlayerRelicState boosted = new PlayerRelicState(
            current.playerId(),
            current.relicId(),
            RelicTier.ASCENSION,
            current.currentEssence(),
            current.essenceByType(),
            List.copyOf(unlocked),
            false
        );
        this.core.savePlayerState(boosted);

        player.sendMessage(ChatColor.GOLD + "[Relicbound] " + ChatColor.YELLOW + "You now have every spell unlocked and are at " + ChatColor.WHITE + RelicTier.ASCENSION.name() + ChatColor.YELLOW + ".");
        return true;
    }
}
