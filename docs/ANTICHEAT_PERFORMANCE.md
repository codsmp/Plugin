Performance Review (initial)

Targets
- 150+ concurrent players
- Minimal TPS impact

Techniques used
- Ring buffers for bounded historical state to avoid retention and allocations.
- Bounded per-player checks using a registry and per-player iteration.
- Check runner scheduled every 2 ticks to batch work.
- Async file logging for persistence.
- Lightweight evidence maps per violation limited to a few fields.

Potential improvements
- Shard check runner threads across cores (careful with Bukkit thread rules).
- Only run expensive checks for players that recently had relevant events (e.g., combat checks only for players in combat).
- Sample checks probabilistically under high load.

Memory estimates
- Each PlayerTracker allocates N snapshots; with defaults (~64 movement history) and modest snapshot size, a 150-player server should remain well within a few hundred MBs of heap if JVM tuned correctly.

*** End of file
