package com.relicbound.paper.anticheat.tracking;

import com.relicbound.paper.anticheat.config.AnticheatConfig;
import com.relicbound.paper.anticheat.util.RingBuffer;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public final class PlayerTracker {
    private final UUID playerId;
    private final RingBuffer<MovementSnapshot> movements;
    private final RingBuffer<CombatSnapshot> combat;
    private final RingBuffer<ClickSnapshot> clicks;
    private final RingBuffer<VelocitySnapshot> velocity;
    private final RingBuffer<BlockActionSnapshot> blockActions;

    private long lastMoveNanoTime;
    private long lastAttackNanoTime;
    private long lastClickNanoTime;
    private long lastGroundNanoTime;
    private long lastAirNanoTime;
    private long lastVelocityNanoTime;
    private long lastTeleportNanoTime;
    private double totalHorizontalVelocity;
    private double totalVerticalVelocity;
    private boolean sprinting;
    private boolean sneaking;
    private boolean inCombat;
    private UUID lastCombatTarget;

    public PlayerTracker(UUID playerId, AnticheatConfig config) {
        this.playerId = playerId;
        this.movements = new RingBuffer<>(config.tracking().movementHistory());
        this.combat = new RingBuffer<>(config.tracking().combatHistory());
        this.clicks = new RingBuffer<>(config.tracking().clickHistory());
        this.velocity = new RingBuffer<>(config.tracking().velocityHistory());
        this.blockActions = new RingBuffer<>(Math.max(16, config.tracking().combatHistory()));
    }

    public UUID playerId() {
        return this.playerId;
    }

    public void recordMovement(MovementSnapshot snapshot) {
        this.movements.add(snapshot);
        this.lastMoveNanoTime = snapshot.nanoTime();
        this.sprinting = snapshot.sprinting();
        this.sneaking = snapshot.sneaking();
        if (snapshot.onGround()) {
            this.lastGroundNanoTime = snapshot.nanoTime();
        } else {
            this.lastAirNanoTime = snapshot.nanoTime();
        }
    }

    public void recordCombat(CombatSnapshot snapshot) {
        this.combat.add(snapshot);
        this.lastAttackNanoTime = snapshot.nanoTime();
        this.lastCombatTarget = snapshot.targetId();
        this.inCombat = true;
    }

    public void recordClick(ClickSnapshot snapshot) {
        this.clicks.add(snapshot);
        this.lastClickNanoTime = snapshot.nanoTime();
    }

    public void recordVelocity(VelocitySnapshot snapshot) {
        this.velocity.add(snapshot);
        this.lastVelocityNanoTime = snapshot.nanoTime();
        this.totalHorizontalVelocity = Math.hypot(snapshot.x(), snapshot.z());
        this.totalVerticalVelocity = snapshot.y();
    }

    public void recordBlockAction(BlockActionSnapshot snapshot) {
        this.blockActions.add(snapshot);
    }

    public void markTeleport(long nanoTime) {
        this.lastTeleportNanoTime = nanoTime;
    }

    public List<MovementSnapshot> movements() {
        return this.movements.snapshot();
    }

    public List<CombatSnapshot> combat() {
        return this.combat.snapshot();
    }

    public List<ClickSnapshot> clicks() {
        return this.clicks.snapshot();
    }

    public List<VelocitySnapshot> velocity() {
        return this.velocity.snapshot();
    }

    public List<BlockActionSnapshot> blockActions() {
        return this.blockActions.snapshot();
    }

    public MovementSnapshot latestMovement() {
        return this.movements.latest();
    }

    public CombatSnapshot latestCombat() {
        return this.combat.latest();
    }

    public long lastMoveNanoTime() {
        return this.lastMoveNanoTime;
    }

    public long lastAttackNanoTime() {
        return this.lastAttackNanoTime;
    }

    public long lastClickNanoTime() {
        return this.lastClickNanoTime;
    }

    public long lastGroundNanoTime() {
        return this.lastGroundNanoTime;
    }

    public long lastAirNanoTime() {
        return this.lastAirNanoTime;
    }

    public long lastVelocityNanoTime() {
        return this.lastVelocityNanoTime;
    }

    public long lastTeleportNanoTime() {
        return this.lastTeleportNanoTime;
    }

    public boolean sprinting() {
        return this.sprinting;
    }

    public boolean sneaking() {
        return this.sneaking;
    }

    public boolean inCombat() {
        return this.inCombat;
    }

    public UUID lastCombatTarget() {
        return this.lastCombatTarget;
    }

    public double averageClickIntervalMillis(int sampleSize) {
        List<ClickSnapshot> snapshot = this.clicks.snapshot();
        if (snapshot.size() < 2) {
            return 0.0D;
        }
        int limit = Math.min(sampleSize, snapshot.size() - 1);
        long total = 0L;
        int counted = 0;
        for (int i = 0; i < limit; i++) {
            long newer = snapshot.get(i).nanoTime();
            long older = snapshot.get(i + 1).nanoTime();
            if (newer <= older) {
                continue;
            }
            total += (newer - older);
            counted++;
        }
        if (counted == 0) {
            return 0.0D;
        }
        return (total / (double) counted) / 1_000_000.0D;
    }

    public double clickVarianceMillis(int sampleSize) {
        List<ClickSnapshot> snapshot = this.clicks.snapshot();
        if (snapshot.size() < 3) {
            return 0.0D;
        }
        int limit = Math.min(sampleSize, snapshot.size() - 1);
        double[] intervals = new double[limit];
        int counted = 0;
        for (int i = 0; i < limit; i++) {
            long newer = snapshot.get(i).nanoTime();
            long older = snapshot.get(i + 1).nanoTime();
            if (newer <= older) {
                continue;
            }
            intervals[counted++] = (newer - older) / 1_000_000.0D;
        }
        if (counted < 3) {
            return 0.0D;
        }
        double mean = 0.0D;
        for (int i = 0; i < counted; i++) {
            mean += intervals[i];
        }
        mean /= counted;
        double variance = 0.0D;
        for (int i = 0; i < counted; i++) {
            double diff = intervals[i] - mean;
            variance += diff * diff;
        }
        return variance / counted;
    }

    public double recentHorizontalDistance(Player player) {
        MovementSnapshot latest = this.latestMovement();
        if (latest == null) {
            Location location = player.getLocation();
            return Math.hypot(location.getX(), location.getZ());
        }
        return Math.hypot(latest.deltaX(), latest.deltaZ());
    }

    public double currentHorizontalVelocity() {
        return this.totalHorizontalVelocity;
    }

    public double currentVerticalVelocity() {
        return this.totalVerticalVelocity;
    }
}