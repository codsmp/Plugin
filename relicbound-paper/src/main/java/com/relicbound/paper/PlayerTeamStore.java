package com.relicbound.paper;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public final class PlayerTeamStore {
    private final JavaPlugin plugin;
    private final File storageFile;
    private final Map<String, TeamRecord> teamsById = new HashMap<>();
    private final Map<String, Set<String>> invitesByTarget = new HashMap<>();
    private final Scoreboard scoreboard;

    public PlayerTeamStore(JavaPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "teams.yml");
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        this.load();
    }

    public synchronized Optional<TeamRecord> findTeamOfPlayer(String playerId) {
        return this.teamsById.values().stream()
            .filter(team -> team.members.contains(playerId))
            .findFirst();
    }

    public synchronized Optional<TeamRecord> findTeamByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }

        String normalized = this.normalizeIdentifier(identifier);
        for (TeamRecord team : this.teamsById.values()) {
            if (team.id.equalsIgnoreCase(normalized)
                || this.normalizeIdentifier(team.name).equalsIgnoreCase(normalized)
                || team.name.equalsIgnoreCase(identifier)) {
                return Optional.of(team);
            }
        }
        return Optional.empty();
    }

    public synchronized TeamRecord createTeam(String ownerId, String teamName, String prefix) {
        if (this.findTeamOfPlayer(ownerId).isPresent()) {
            throw new IllegalStateException("You are already in a team.");
        }

        String displayName = this.cleanDisplayName(teamName);
        if (displayName.isBlank()) {
            throw new IllegalStateException("Team name cannot be blank.");
        }

        String id = this.generateUniqueTeamId(displayName);
        String scoreboardName = this.generateScoreboardName();
        String visiblePrefix = this.cleanPrefix(prefix.isBlank() ? displayName : prefix);
        TeamRecord record = new TeamRecord(id, scoreboardName, displayName, visiblePrefix, ChatColor.WHITE.name(), ownerId);
        record.members.add(ownerId);
        this.teamsById.put(record.id, record);
        this.persist();
        this.syncAllOnlinePlayers();
        return record;
    }

    public synchronized boolean toggleMember(String ownerId, String targetId) {
        TeamRecord team = this.requireOwnedTeam(ownerId);
        if (team.ownerId.equals(targetId)) {
            throw new IllegalStateException("You cannot remove the team owner.");
        }

        Optional<TeamRecord> existing = this.findTeamOfPlayer(targetId);
        if (existing.isPresent() && existing.get() != team) {
            throw new IllegalStateException("That player is already in another team.");
        }

        boolean added;
        if (team.members.contains(targetId)) {
            team.members.remove(targetId);
            added = false;
        } else {
            team.members.add(targetId);
            added = true;
        }

        this.clearInvitesForPlayer(targetId);

        this.persist();
        this.syncAllOnlinePlayers();
        return added;
    }

    public synchronized void inviteMember(String ownerId, String targetId) {
        TeamRecord team = this.requireOwnedTeam(ownerId);
        if (team.ownerId.equals(targetId)) {
            throw new IllegalStateException("You cannot invite yourself.");
        }
        if (this.findTeamOfPlayer(targetId).isPresent()) {
            throw new IllegalStateException("That player is already in a team.");
        }

        this.invitesByTarget.computeIfAbsent(targetId, ignored -> new HashSet<>()).add(team.id);
        this.persist();
    }

    public synchronized List<TeamRecord> getInvitesForPlayer(String playerId) {
        return this.invitesByTarget.getOrDefault(playerId, Set.of()).stream()
            .map(this.teamsById::get)
            .filter(team -> team != null)
            .toList();
    }

    public synchronized TeamRecord acceptInvite(String playerId, String teamIdentifier) {
        if (this.findTeamOfPlayer(playerId).isPresent()) {
            throw new IllegalStateException("You are already in a team.");
        }

        TeamRecord team = this.resolveInvitedTeam(playerId, teamIdentifier);
        team.members.add(playerId);
        this.clearInvitesForPlayer(playerId);
        this.persist();
        this.syncAllOnlinePlayers();
        return team;
    }

    public synchronized boolean denyInvite(String playerId, String teamIdentifier) {
        Set<String> invites = this.invitesByTarget.get(playerId);
        if (invites == null || invites.isEmpty()) {
            throw new IllegalStateException("You do not have any team invites.");
        }

        TeamRecord team = this.resolveInvitedTeam(playerId, teamIdentifier);
        boolean removed = invites.remove(team.id);
        if (invites.isEmpty()) {
            this.invitesByTarget.remove(playerId);
        }
        this.persist();
        return removed;
    }

    public synchronized boolean toggleAlliance(String ownerId, String targetIdentifier) {
        TeamRecord team = this.requireOwnedTeam(ownerId);
        TeamRecord target = this.findTeamByIdentifier(targetIdentifier)
            .orElseThrow(() -> new IllegalStateException("That team does not exist."));
        if (team.id.equalsIgnoreCase(target.id)) {
            throw new IllegalStateException("You cannot ally your own team.");
        }

        boolean added;
        if (team.allies.contains(target.id)) {
            team.allies.remove(target.id);
            target.allies.remove(team.id);
            added = false;
        } else {
            team.allies.add(target.id);
            target.allies.add(team.id);
            added = true;
        }

        this.persist();
        this.syncAllOnlinePlayers();
        return added;
    }

    public synchronized void setPrefix(String ownerId, String prefix) {
        TeamRecord team = this.requireOwnedTeam(ownerId);
        team.prefix = this.cleanPrefix(prefix);
        this.persist();
        this.syncAllOnlinePlayers();
    }

    public synchronized void setColor(String ownerId, String colorName) {
        TeamRecord team = this.requireOwnedTeam(ownerId);
        team.color = this.normalizeColor(colorName);
        this.persist();
        this.syncAllOnlinePlayers();
    }

    public synchronized void leaveTeam(String playerId) {
        TeamRecord team = this.findTeamOfPlayer(playerId)
            .orElseThrow(() -> new IllegalStateException("You are not in a team."));
        if (team.ownerId.equals(playerId)) {
            throw new IllegalStateException("Team owners must disband the team instead of leaving.");
        }
        team.members.remove(playerId);
        this.persist();
        this.syncAllOnlinePlayers();
    }

    public synchronized void disbandTeam(String ownerId) {
        TeamRecord team = this.requireOwnedTeam(ownerId);
        this.teamsById.remove(team.id);
        for (TeamRecord other : this.teamsById.values()) {
            other.allies.remove(team.id);
        }
        for (Set<String> invites : this.invitesByTarget.values()) {
            invites.remove(team.id);
        }
        this.invitesByTarget.entrySet().removeIf(entry -> entry.getValue().isEmpty());
        Team bukkitTeam = this.scoreboard.getTeam(team.scoreboardName);
        if (bukkitTeam != null) {
            bukkitTeam.unregister();
        }
        this.persist();
        this.syncAllOnlinePlayers();
    }

    public synchronized boolean isAlliedOrSame(String firstPlayerId, String secondPlayerId) {
        Optional<TeamRecord> first = this.findTeamOfPlayer(firstPlayerId);
        Optional<TeamRecord> second = this.findTeamOfPlayer(secondPlayerId);
        if (first.isEmpty() || second.isEmpty()) {
            return false;
        }
        if (first.get().id.equalsIgnoreCase(second.get().id)) {
            return true;
        }
        return first.get().allies.contains(second.get().id) || second.get().allies.contains(first.get().id);
    }

    public synchronized void syncAllOnlinePlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            this.syncPlayer(player);
        }
    }

    public synchronized void syncPlayer(Player player) {
        if (this.scoreboard != null && player.getScoreboard() != this.scoreboard) {
            player.setScoreboard(this.scoreboard);
        }

        for (TeamRecord record : this.teamsById.values()) {
            Team team = this.ensureScoreboardTeam(record);
            team.removeEntry(player.getName());
        }

        Optional<TeamRecord> team = this.findTeamOfPlayer(player.getUniqueId().toString());
        if (team.isPresent()) {
            this.ensureScoreboardTeam(team.get()).addEntry(player.getName());
        }
    }

    public synchronized void clearInvitesForPlayer(String playerId) {
        this.invitesByTarget.remove(playerId);
    }

    public synchronized Optional<TeamRecord> getTeamForPlayer(String playerId) {
        return this.findTeamOfPlayer(playerId);
    }

    private TeamRecord requireOwnedTeam(String ownerId) {
        TeamRecord team = this.findTeamOfPlayer(ownerId)
            .orElseThrow(() -> new IllegalStateException("You do not have a team yet. Use /team create <name> first."));
        if (!team.ownerId.equals(ownerId)) {
            throw new IllegalStateException("Only the team owner can manage that.");
        }
        return team;
    }

    private Team ensureScoreboardTeam(TeamRecord record) {
        Team team = this.scoreboard.getTeam(record.scoreboardName);
        if (team == null) {
            team = this.scoreboard.registerNewTeam(record.scoreboardName);
        }
        team.setPrefix(this.formatPrefix(record));
        team.setAllowFriendlyFire(false);
        return team;
    }

    private String formatPrefix(TeamRecord record) {
        ChatColor color = this.colorFromName(record.color);
        return ChatColor.GRAY + "[" + color + record.prefix + ChatColor.GRAY + "] " + ChatColor.RESET;
    }

    private void load() {
        if (!this.storageFile.exists()) {
            return;
        }

        YamlConfiguration configuration = YamlConfiguration.loadConfiguration(this.storageFile);
        if (configuration.getConfigurationSection("teams") == null) {
            return;
        }

        for (String id : configuration.getConfigurationSection("teams").getKeys(false)) {
            String basePath = "teams." + id + ".";
            String scoreboardName = configuration.getString(basePath + "scoreboard-name", this.generateScoreboardName());
            String name = configuration.getString(basePath + "name", id);
            String prefix = configuration.getString(basePath + "prefix", name);
            String color = configuration.getString(basePath + "color", ChatColor.WHITE.name());
            String ownerId = configuration.getString(basePath + "owner-id", "");
            if (ownerId.isBlank()) {
                continue;
            }

            TeamRecord record = new TeamRecord(id, scoreboardName, name, prefix, color, ownerId);
            record.members.addAll(configuration.getStringList(basePath + "members"));
            record.allies.addAll(configuration.getStringList(basePath + "allies"));
            record.members.add(ownerId);
            this.teamsById.put(record.id, record);
        }

        if (configuration.getConfigurationSection("invites") != null) {
            for (String targetId : configuration.getConfigurationSection("invites").getKeys(false)) {
                this.invitesByTarget.put(targetId, new HashSet<>(configuration.getStringList("invites." + targetId + ".teams")));
            }
        }
    }

    private void persist() {
        YamlConfiguration configuration = new YamlConfiguration();
        for (TeamRecord record : this.teamsById.values()) {
            String basePath = "teams." + record.id + ".";
            configuration.set(basePath + "scoreboard-name", record.scoreboardName);
            configuration.set(basePath + "name", record.name);
            configuration.set(basePath + "prefix", record.prefix);
            configuration.set(basePath + "color", record.color);
            configuration.set(basePath + "owner-id", record.ownerId);
            configuration.set(basePath + "members", new ArrayList<>(record.members));
            configuration.set(basePath + "allies", new ArrayList<>(record.allies));
        }

        for (Map.Entry<String, Set<String>> entry : this.invitesByTarget.entrySet()) {
            configuration.set("invites." + entry.getKey() + ".teams", new ArrayList<>(entry.getValue()));
        }

        try {
            this.storageFile.getParentFile().mkdirs();
            configuration.save(this.storageFile);
        } catch (IOException exception) {
            this.plugin.getLogger().severe("Failed to save team data: " + exception.getMessage());
        }
    }

    private String generateUniqueTeamId(String name) {
        String base = this.normalizeIdentifier(name);
        if (base.isBlank()) {
            base = "team";
        }

        String candidate = base;
        int suffix = 2;
        while (this.teamsById.containsKey(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String generateScoreboardName() {
        String candidate;
        do {
            candidate = "rb_" + UUID.randomUUID().toString().replace("-", "").substring(0, 10);
            String finalCandidate = candidate;
            if (this.scoreboard.getTeam(finalCandidate) == null && this.teamsById.values().stream().noneMatch(team -> team.scoreboardName.equals(finalCandidate))) {
                break;
            }
        } while (true);
        return candidate;
    }

    private TeamRecord resolveInvitedTeam(String playerId, String teamIdentifier) {
        Set<String> invitedTeamIds = this.invitesByTarget.getOrDefault(playerId, Set.of());
        if (invitedTeamIds.isEmpty()) {
            throw new IllegalStateException("You do not have any team invites.");
        }

        if (teamIdentifier == null || teamIdentifier.isBlank()) {
            if (invitedTeamIds.size() == 1) {
                String teamId = invitedTeamIds.iterator().next();
                TeamRecord team = this.teamsById.get(teamId);
                if (team == null) {
                    throw new IllegalStateException("That invite is no longer valid.");
                }
                return team;
            }

            throw new IllegalStateException("You have multiple invites. Use /team accept <team>.");
        }

        TeamRecord team = this.findTeamByIdentifier(teamIdentifier)
            .orElseThrow(() -> new IllegalStateException("That team does not exist."));
        if (!invitedTeamIds.contains(team.id)) {
            throw new IllegalStateException("You were not invited to that team.");
        }
        return team;
    }

    private String normalizeIdentifier(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_]+", "");
    }

    private String cleanDisplayName(String value) {
        return value == null ? "" : value.trim();
    }

    private String cleanPrefix(String value) {
        String prefix = value == null ? "" : ChatColor.translateAlternateColorCodes('&', value.trim());
        return prefix.isBlank() ? "Team" : prefix;
    }

    private String normalizeColor(String colorName) {
        return this.colorFromName(colorName).name();
    }

    private ChatColor colorFromName(String colorName) {
        if (colorName == null || colorName.isBlank()) {
            return ChatColor.WHITE;
        }

        String normalized = colorName.trim().toUpperCase(Locale.ROOT).replace(' ', '_');
        try {
            ChatColor color = ChatColor.valueOf(normalized);
            return color.isColor() ? color : ChatColor.WHITE;
        } catch (IllegalArgumentException exception) {
            return ChatColor.WHITE;
        }
    }

    public static final class TeamRecord {
        private final String id;
        private final String scoreboardName;
        private final String name;
        private final String ownerId;
        private String color;
        private final Set<String> members = new HashSet<>();
        private final Set<String> allies = new HashSet<>();
        private String prefix;

        private TeamRecord(String id, String scoreboardName, String name, String prefix, String color, String ownerId) {
            this.id = id;
            this.scoreboardName = scoreboardName;
            this.name = name;
            this.prefix = prefix;
            this.color = color;
            this.ownerId = ownerId;
        }

        public String id() {
            return this.id;
        }

        public String scoreboardName() {
            return this.scoreboardName;
        }

        public String name() {
            return this.name;
        }

        public String ownerId() {
            return this.ownerId;
        }

        public String prefix() {
            return this.prefix;
        }

        public String color() {
            return this.color;
        }

        public List<String> members() {
            return List.copyOf(this.members);
        }

        public List<String> allies() {
            return List.copyOf(this.allies);
        }
    }
}