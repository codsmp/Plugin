package com.relicbound.paper;

import com.relicbound.paper.PlayerTeamStore.TeamRecord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TeamCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final PlayerTeamStore teamStore;

    public TeamCommand(JavaPlugin plugin, PlayerTeamStore teamStore) {
        this.plugin = plugin;
        this.teamStore = teamStore;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Players only.");
                return true;
            }

            if (args.length == 0 || "help".equalsIgnoreCase(args[0])) {
                this.sendHelp(player);
                return true;
            }

            String action = args[0].toLowerCase(Locale.ROOT);
            return switch (action) {
                case "create" -> this.createTeam(player, args);
                case "prefix" -> this.setPrefix(player, args);
                case "color" -> this.setColor(player, args);
                case "trust" -> this.toggleMember(player, args);
                case "ally" -> this.toggleAlliance(player, args);
                case "disband" -> this.disband(player);
                case "leave" -> this.leave(player);
                case "info" -> this.info(player, args);
                default -> {
                    this.sendHelp(player);
                    yield true;
                }
            };
        } catch (IllegalStateException exception) {
            sender.sendMessage(ChatColor.RED + exception.getMessage());
            return true;
        } catch (Throwable throwable) {
            this.plugin.getLogger().severe("Error executing /team command: " + throwable.getMessage());
            sender.sendMessage(ChatColor.RED + "An unexpected error occurred while executing that command.");
            return true;
        }
    }

    private boolean createTeam(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team create <name> [prefix]");
            return true;
        }

        String name = args[1];
        String prefix = args.length > 2 ? String.join(" ", List.of(args).subList(2, args.length)) : name;
        TeamRecord team = this.teamStore.createTeam(player.getUniqueId().toString(), name, prefix);
        player.sendMessage(ChatColor.GREEN + "Created team " + ChatColor.WHITE + team.name() + ChatColor.GREEN + ".");
        player.sendMessage(ChatColor.GRAY + "Use /team prefix <text> to change how it appears before your name.");
        return true;
    }

    private boolean setPrefix(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team prefix <text>");
            return true;
        }

        String prefix = String.join(" ", List.of(args).subList(1, args.length));
        this.teamStore.setPrefix(player.getUniqueId().toString(), prefix);
        player.sendMessage(ChatColor.GREEN + "Updated your team prefix.");
        return true;
    }

    private boolean setColor(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team color <color>");
            player.sendMessage(ChatColor.GRAY + "Examples: red, blue, green, gold, aqua, white");
            return true;
        }

        this.teamStore.setColor(player.getUniqueId().toString(), args[1]);
        player.sendMessage(ChatColor.GREEN + "Updated your team color.");
        return true;
    }

    private boolean toggleMember(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team trust <player>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            player.sendMessage(ChatColor.RED + "That player must be online.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage(ChatColor.RED + "You cannot add yourself to your own team.");
            return true;
        }

        boolean added = this.teamStore.toggleMember(player.getUniqueId().toString(), target.getUniqueId().toString());
        if (added) {
            player.sendMessage(ChatColor.GREEN + "Added " + target.getName() + " to your team.");
            target.sendMessage(ChatColor.AQUA + "You were added to " + player.getName() + "'s team.");
        } else {
            player.sendMessage(ChatColor.YELLOW + "Removed " + target.getName() + " from your team.");
            target.sendMessage(ChatColor.YELLOW + "You were removed from " + player.getName() + "'s team.");
        }
        return true;
    }

    private boolean toggleAlliance(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /team ally <team>");
            return true;
        }

        boolean added = this.teamStore.toggleAlliance(player.getUniqueId().toString(), args[1]);
        player.sendMessage(added
            ? ChatColor.GREEN + "Your team is now allied with " + args[1] + "."
            : ChatColor.YELLOW + "Your team is no longer allied with " + args[1] + ".");
        return true;
    }

    private boolean disband(Player player) {
        this.teamStore.disbandTeam(player.getUniqueId().toString());
        player.sendMessage(ChatColor.YELLOW + "Your team has been disbanded.");
        return true;
    }

    private boolean leave(Player player) {
        this.teamStore.leaveTeam(player.getUniqueId().toString());
        player.sendMessage(ChatColor.YELLOW + "You left your team.");
        return true;
    }

    private boolean info(Player player, String[] args) {
        TeamRecord team;
        if (args.length < 2) {
            team = this.teamStore.getTeamForPlayer(player.getUniqueId().toString()).orElse(null);
        } else {
            team = this.teamStore.findTeamByIdentifier(args[1]).orElse(null);
        }

        if (team == null) {
            player.sendMessage(ChatColor.RED + "No team found.");
            return true;
        }

        player.sendMessage(ChatColor.GOLD + "[Team] " + ChatColor.YELLOW + team.name());
        player.sendMessage(ChatColor.GRAY + "Prefix: " + ChatColor.WHITE + team.prefix());
        player.sendMessage(ChatColor.GRAY + "Owner: " + ChatColor.WHITE + this.resolvePlayerName(team.ownerId()));
        player.sendMessage(ChatColor.GRAY + "Members: " + ChatColor.WHITE + this.formatPlayerList(team.members()));
        player.sendMessage(ChatColor.GRAY + "Allies: " + ChatColor.WHITE + this.formatTeamList(team.allies()));
        return true;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "[Team] " + ChatColor.YELLOW + "Commands:");
        player.sendMessage(ChatColor.GRAY + "/team create <name> [prefix]");
        player.sendMessage(ChatColor.GRAY + "/team prefix <text>");
        player.sendMessage(ChatColor.GRAY + "/team color <color>");
        player.sendMessage(ChatColor.GRAY + "/team trust <online player>");
        player.sendMessage(ChatColor.GRAY + "/team ally <team>");
        player.sendMessage(ChatColor.GRAY + "/team leave");
        player.sendMessage(ChatColor.GRAY + "/team disband");
        player.sendMessage(ChatColor.GRAY + "/team info [team]");
    }

    private String resolvePlayerName(String playerId) {
        try {
            Player online = Bukkit.getPlayer(java.util.UUID.fromString(playerId));
            if (online != null) {
                return online.getName();
            }
        } catch (IllegalArgumentException ignored) {
        }
        return playerId.length() > 8 ? playerId.substring(0, 8) : playerId;
    }

    private String formatPlayerList(List<String> playerIds) {
        if (playerIds.isEmpty()) {
            return ChatColor.DARK_GRAY + "none";
        }
        List<String> names = new ArrayList<>();
        for (String playerId : playerIds) {
            names.add(this.resolvePlayerName(playerId));
        }
        return String.join(ChatColor.GRAY + ", " + ChatColor.WHITE, names);
    }

    private String formatTeamList(List<String> teamIds) {
        if (teamIds.isEmpty()) {
            return ChatColor.DARK_GRAY + "none";
        }
        List<String> names = new ArrayList<>();
        for (String teamId : teamIds) {
            this.teamStore.findTeamByIdentifier(teamId).ifPresentOrElse(
                team -> names.add(team.name()),
                () -> names.add(teamId)
            );
        }
        return String.join(ChatColor.GRAY + ", " + ChatColor.WHITE, names);
    }
}